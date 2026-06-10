# Informe de Paralelización

**Fundamentos de Programación Funcional y Concurrente**
**Integrantes:**

Maria Fernanda Betancourt Montoya

Kevin Andres Rosero Romo

---

## Estrategia de paralelización

### `choquesPar`

Se divide el vector de índices en dos mitades: la mitad izquierda contiene los
índices $0$ hasta $\lfloor n/2 \rfloor - 1$ y la mitad derecha los índices
$\lfloor n/2 \rfloor$ hasta $n-1$. Cada mitad genera sus pares $(i, j)$ con
$j > i$ y cuenta los choques de forma independiente usando `parallel`. Al final
se suman los dos resultados.

```scala
val (choquesIzq, choquesDer) = parallel(
  paresIzq.count { case (i, j) =>
    a(i) >= 0 && a(i) == a(j) && solapan(cursos(i), cursos(j))
  },
  paresDer.count { case (i, j) =>
    a(i) >= 0 && a(i) == a(j) && solapan(cursos(i), cursos(j))
  }
)
choquesIzq + choquesDer
```

### `desperdicioPar`

Se divide el vector de cursos en dos mitades. Cada mitad calcula la suma de
desperdicios de sus cursos de forma independiente usando `parallel`. Al final
se suman los dos resultados.

```scala
val (desperdicioIzq, desperdicioDer) = parallel(
  indicesIzq.map { i => ... }.sum,
  indicesDer.map { i => ... }.sum
)
desperdicioIzq + desperdicioDer
```

### `movilidadPar`

Se ordenan los cursos asignados por hora de inicio y se divide la secuencia en
dos mitades. Cada mitad calcula la suma de distancias entre sus pares consecutivos
en paralelo. Al final se suman los dos resultados más la distancia entre el último
curso de la mitad izquierda y el primero de la mitad derecha, que es el par de la
juntura que no queda en ninguna mitad.

```scala
val (movIzq, movDer) = parallel(
  consIzq.map { case (i, j) => d(a(i))(a(j)) }.sum,
  consDer.map { case (i, j) => d(a(i))(a(j)) }.sum
)
movIzq + movDer + d(a(asignados(mid - 1)))(a(asignados(mid)))
```

### `generarAsignacionesPar`

Se divide el rango de aulas $\{0, \ldots, m-1\}$ en dos mitades. Cada mitad
construye las asignaciones que empiezan con sus aulas usando `parallel` y llama
a `generarAsignaciones` para construir el resto. Al final se concatenan los dos
vectores con `++`.

```scala
val (izq, der) = parallel(
  aulasIzq.flatMap { aula =>
    generarAsignaciones(n - 1, m).map(resto => aula +: resto)
  },
  aulasDer.flatMap { aula =>
    generarAsignaciones(n - 1, m).map(resto => aula +: resto)
  }
)
izq ++ der
```

### `asignacionOptimaPar`

Se generan todas las asignaciones con `generarAsignacionesPar` y se dividen en
dos mitades. Cada mitad busca su mínimo local en paralelo con `parallel`. Al
final se comparan los dos mínimos y se devuelve el menor.

```scala
val (minimoIzq, minimoDer) = parallel(
  mitadIzq.map(a => (a, costoAsignacion(...))).minBy(_._2),
  mitadDer.map(a => (a, costoAsignacion(...))).minBy(_._2)
)
if (minimoIzq._2 <= minimoDer._2) minimoIzq else minimoDer
```

---

## Resultados experimentales

Los tiempos fueron medidos usando `org.scalameter` con la siguiente estructura:

```scala
import org.scalameter._
val timeSeq = measure { asignacionOptima(cursos, aulas, d, w) }
val timePar = measure { asignacionOptimaPar(cursos, aulas, d, w) }
println(s"Secuencial: $timeSeq ms")
println(s"Paralelo:   $timePar ms")
```

| Cursos $n$ | Aulas $m$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------:|:---------------:|:-------------:|:---------------:|
| 4          | 3         | 33.01           | 15.86         | 51.95           |
| 6          | 4         | 76.65           | 31.36         | 59.08           |
| 7          | 5         | 362.87          | 144.32        | 60.23           |
| 8          | 5         | 995.23          | 535.36        | 46.21           |

---

## Análisis con la ley de Amdahl

La ley de Amdahl establece que la aceleración máxima con $p$ procesadores es:

$$S(p) = \frac{1}{(1 - \alpha) + \frac{\alpha}{p}}$$

donde $\alpha$ es la fracción del programa que se puede paralelizar.

### Fracción paralelizada en cada función

- **`choquesPar`**: la generación y conteo de pares es completamente paralelizable.
  La única parte secuencial es la suma final `choquesIzq + choquesDer`, que es
  una operación constante. Por lo tanto $\alpha \approx 1$.

- **`desperdicioPar`**: el cálculo del desperdicio por curso es independiente entre
  cursos, por lo que la parte paralelizable es el `map` y el `sum` de cada mitad.
  La suma final es constante. Por lo tanto $\alpha \approx 1$.

- **`movilidadPar`**: el ordenamiento de cursos es secuencial y no se puede
  paralelizar. Solo el cálculo de distancias de cada mitad es paralelo. Por lo
  tanto $\alpha < 1$ y la ganancia es más limitada.

- **`generarAsignacionesPar`**: la construcción de las asignaciones de cada mitad
  es independiente. Sin embargo la llamada interna a `generarAsignaciones`
  secuencial dentro de cada rama limita la fracción paralelizable.

- **`asignacionOptimaPar`**: la evaluación del costo de cada asignación es
  independiente, por lo que casi todo el trabajo es paralelizable.
  Por lo tanto $\alpha \approx 1$ para entradas grandes.

### Pares $(n, m)$ donde el paralelismo genera ganancias

Con $p = 2$ procesadores y $\alpha \approx 1$ la ley de Amdahl predice una
aceleración máxima teórica de:

$$S(2) = \frac{1}{(1 - 1) + \frac{1}{2}} = 2$$

Es decir, en el mejor caso la versión paralela puede ser hasta 2 veces más rápida
que la secuencial. Los resultados experimentales muestran aceleraciones entre
46% y 60%, lo cual es consistente con esta predicción considerando la sobrecarga
de sincronización entre hilos.

En todos los casos medidos el paralelismo generó ganancias positivas, incluso
para $n=4$ y $m=3$ donde la aceleración fue del 51.95%. Esto indica que en la
máquina usada el costo de crear y sincronizar los hilos es pequeño comparado
con el trabajo a realizar incluso para entradas pequeñas.

---

## Conclusiones de paralelización

Los resultados muestran que el paralelismo fue beneficioso en todos los casos
medidos, con aceleraciones entre 46% y 60%. La mayor aceleración se obtuvo con
$n=7$ y $m=5$ con un 60.23%, donde el espacio de búsqueda de $5^7 = 78125$
asignaciones es suficientemente grande para que ambos hilos tengan trabajo
significativo.

La función que más se benefició del paralelismo fue `asignacionOptimaPar` porque
evalúa un número muy grande de asignaciones de forma independiente, lo que permite
dividir el trabajo casi perfectamente entre los dos hilos. La función con menor
ganancia potencial fue `movilidadPar` porque el ordenamiento previo de los cursos
es secuencial.

En general, el paralelismo es más útil cuando el trabajo a realizar es grande,
independiente entre las partes y el costo de combinar los resultados es pequeño
comparado con el trabajo total. Para este problema, con $n \geq 4$ y $m \geq 3$
ya se obtienen ganancias significativas en la máquina utilizada.