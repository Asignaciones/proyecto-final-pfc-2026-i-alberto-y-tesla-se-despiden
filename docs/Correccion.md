# Informe de Corrección

**Fundamentos de Programación Funcional y Concurrente**
**Integrantes:**

Maria Fernanda Betancourt Montoya

Kevin Andres Rosero Romo


---

## Argumentación de corrección de programas

### `solapan`

**Especificación:** Dados dos cursos $c_1$ y $c_2$, `solapan` devuelve `true` si y
solo si los intervalos $[\text{ini}(c_1), \text{fin}(c_1))$ y
$[\text{ini}(c_2), \text{fin}(c_2))$ tienen intersección no vacía.

La condición estándar de solapamiento de intervalos es:

$$
\text{solapan}(c_1, c_2) \iff \text{ini}(c_1) < \text{fin}(c_2) \land \text{ini}(c_2) < \text{fin}(c_1)
$$

**Argumentación:** La implementación evalúa exactamente esta condición:

```scala
iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)
```

Dos intervalos **no** se solapan cuando uno termina antes de que el otro empiece:

$$
\text{fin}(c_1) \leq \text{ini}(c_2) \lor \text{fin}(c_2) \leq \text{ini}(c_1)
$$

Negando esta condición por la ley de De Morgan se obtiene exactamente la condición
implementada. Por lo tanto la implementación es correcta.

---

### `choques`

**Especificación:** El número de choques de una asignación $\alpha$ es:

$$
CH_C^\alpha = |\{(i,j) \mid 0 \leq i < j < n,\ \alpha_i = \alpha_j,\ \alpha_i \geq 0,\ c_i \text{ solapa con } c_j\}|
$$

**Argumentación:** La implementación genera todos los pares $(i, j)$ con $i < j$
usando:

```scala
indices.flatMap { i =>
  indices.filter(j => j > i).map(j => (i, j))
}
```

La condición `j > i` garantiza que cada par se genera exactamente una vez, lo que
corresponde al conjunto $\{(i,j) \mid i < j\}$ de la especificación. Luego `count`
verifica las tres condiciones:

- $\alpha_i \geq 0$: el curso $i$ está asignado.
- $\alpha_i = \alpha_j$: ambos cursos están en la misma aula.
- $\text{solapan}(c_i, c_j)$: los horarios se cruzan.

Estas tres condiciones corresponden exactamente a la definición de $CH_C^\alpha$,
por lo tanto la implementación es correcta.

---

### `capacidadFallida`

**Especificación:** El número de cursos con capacidad fallida es:

$$
CF_{C,A}^\alpha = |\{i \mid \alpha_i \geq 0 \land \text{cap}(a_{\alpha_i}) < \text{est}(c_i)\}|
$$

**Argumentación:** La implementación usa `count` sobre todos los índices y verifica:

```scala
a(i) >= 0 && capAula(aulas(a(i))) < estCurso(cursos(i))
```

Esto corresponde exactamente a contar los índices $i$ tales que el curso está
asignado ($\alpha_i \geq 0$) y la capacidad del aula es menor que el número de
estudiantes ($\text{cap}(a_{\alpha_i}) < \text{est}(c_i)$), que es la definición
de $CF_{C,A}^\alpha$. Por lo tanto la implementación es correcta.

---

### `desperdicio`

**Especificación:** El desperdicio total de capacidad es:

$$
DE_{C,A}^\alpha = \sum_{\substack{i=0 \\ \alpha_i \geq 0}}^{n-1} \max(\text{cap}(a_{\alpha_i}) - \text{est}(c_i),\ 0)
$$

donde el término es $0$ cuando $\text{cap}(a_{\alpha_i}) < \text{est}(c_i)$.

**Argumentación:** La implementación usa `map` para calcular el aporte de cada
curso y `.sum` para sumarlos:

```scala
if (a(i) >= 0 && capAula(aulas(a(i))) >= estCurso(cursos(i)))
  capAula(aulas(a(i))) - estCurso(cursos(i))
else 0
```

Cuando $\text{cap}(a_{\alpha_i}) \geq \text{est}(c_i)$ el aporte es la diferencia
positiva, y cuando no se cumple el aporte es $0$, lo que corresponde exactamente
a $\max(\text{cap}(a_{\alpha_i}) - \text{est}(c_i), 0)$. Por lo tanto la
implementación es correcta.

---

### `movilidad`

**Especificación:** Sea $\sigma$ el ordenamiento de los cursos asignados por hora
de inicio. El costo de movilidad es:

$$
MV_{C,A,D}^\alpha = \sum_{j=0}^{k-2} D[\alpha_{\sigma_j}, \alpha_{\sigma_{j+1}}]
$$

**Argumentación:** La implementación primero filtra los cursos asignados y los
ordena por hora de inicio:

```scala
val asignados = cursos.indices.toVector
  .filter(i => a(i) >= 0)
  .sortBy(i => iniCurso(cursos(i)))
```

Luego forma los pares consecutivos con `zip` y `drop(1)`:

```scala
asignados.zip(asignados.drop(1))
```

Esto produce exactamente los pares $(\sigma_j, \sigma_{j+1})$ para
$j = 0, \ldots, k-2$. Para cada par se busca la distancia $D[\alpha_{\sigma_j},
\alpha_{\sigma_{j+1}}]$ y se suman con `.sum`. Esto corresponde exactamente a
la definición de $MV_{C,A,D}^\alpha$. Por lo tanto la implementación es correcta.

---

### `costoAsignacion`

**Especificación:** El costo total de una asignación es:

$$
CT_{C,A,D}^\alpha = w_{CH} \cdot CH_C^\alpha + w_{CF} \cdot CF_{C,A}^\alpha + w_{DE} \cdot DE_{C,A}^\alpha + w_{MV} \cdot MV_{C,A,D}^\alpha
$$

**Argumentación:** La implementación desempaca los pesos y combina las cuatro
funciones:

```scala
val (wCH, wCF, wDE, wMV) = w
wCH * choques(cursos, a) +
  wCF * capacidadFallida(cursos, aulas, a) +
  wDE * desperdicio(cursos, aulas, a) +
  wMV * movilidad(cursos, aulas, d, a)
```

Dado que se argumentó la corrección de `choques`, `capacidadFallida`, `desperdicio`
y `movilidad` por separado, y la implementación las combina exactamente como indica
la fórmula $CT_{C,A,D}^\alpha$, la implementación es correcta.

---

### `generarAsignaciones`

**Especificación:** Sea $G(n, m)$ el conjunto de todos los vectores en
$\{0, \ldots, m-1\}^n$. La función debe devolver un vector con exactamente
$m^n$ asignaciones completas.

$$
G(n, m) = \{\alpha \mid \alpha \in \{0, \ldots, m-1\}^n\}
$$

**Programa:**

```scala
def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = {
  def aux(k: Int): Vector[Asignacion] =
    if (k == 0) Vector(Vector.empty)
    else
      aux(k - 1).flatMap { resto =>
        (0 until m).toVector.map(aula => resto :+ aula)
      }
  aux(n)
}
```

**Demostración por inducción estructural sobre $k$:**

Queremos demostrar que:

$$
\forall k \in \mathbb{N} : \text{aux}(k) == G(k, m)
$$

- **Caso base:** $k = 0$

$$
\text{aux}(0) \to \text{Vector}(\text{Vector.empty})
$$

$G(0, m)$ contiene exactamente un vector de longitud $0$, que es el vector vacío.
Por lo tanto $\text{aux}(0) == G(0, m)$. ✓

- **Caso inductivo:** $k = p + 1$, $p \geq 0$

Hipótesis de inducción: $\text{aux}(p) == G(p, m)$.

$$
\text{aux}(p+1) \to \text{aux}(p)\text{.flatMap} \{ \text{resto} \Rightarrow (0 \text{ until } m)\text{.map}(\text{aula} \Rightarrow \text{resto} \mathrel{:+} \text{aula}) \}
$$

Por hipótesis de inducción, `aux(p)` contiene todos los vectores de longitud $p$
sobre $\{0, \ldots, m-1\}$. Para cada uno de esos vectores se agregan todas las
aulas posibles $\{0, \ldots, m-1\}$ al final con `:+`, generando $m$ nuevos vectores
de longitud $p+1$ por cada vector de longitud $p$. Como hay $m^p$ vectores de
longitud $p$, el resultado tiene $m^p \cdot m = m^{p+1}$ vectores, todos distintos
y de longitud $p+1$, que es exactamente $G(p+1, m)$.

Por lo tanto $\text{aux}(p+1) == G(p+1, m)$. ✓

**Conclusión:**

$$
\forall k \in \mathbb{N} : \text{aux}(k) == G(k, m)
$$

En particular, `generarAsignaciones(n, m)` $=$ `aux(n)` $== G(n, m)$, por lo tanto
la implementación es correcta.

---

### `asignacionOptima`

**Especificación:** Dados $C$, $A$, $D$ y $w$, devuelve la asignación $\alpha^*$ tal que:

$$
\alpha^* = \arg\min_{\alpha \in G(n,m)} CT_{C,A,D}^\alpha
$$

**Argumentación:** La implementación genera todas las asignaciones posibles con
`generarAsignaciones`, calcula el costo de cada una con `costoAsignacion` y
devuelve la de menor costo con `minBy`:

```scala
generarAsignaciones(cursos.length, aulas.length)
  .map(a => (a, costoAsignacion(cursos, aulas, d, a, w)))
  .minBy(_._2)
```

Dado que se demostró que `generarAsignaciones` produce exactamente $G(n, m)$ y
que `costoAsignacion` calcula correctamente $CT_{C,A,D}^\alpha$, y dado que
`minBy` devuelve el elemento con el menor valor del segundo componente de la tupla,
la implementación devuelve exactamente $\arg\min_{\alpha \in G(n,m)} CT_{C,A,D}^\alpha$.
Por lo tanto la implementación es correcta.

---

## Casos de prueba

Los casos de prueba se encuentran en
`app/src/test/scala/proyecto/AsignacionAulasTest.scala` y
`app/src/test/scala/proyecto/AsignacionAulasParTest.scala` y se ejecutan
automáticamente con `./gradlew test`.

Se incluyen mínimo 5 casos de prueba por función, cubriendo:

- **`solapan`**: cursos que se solapan, que no se solapan, adyacentes, idénticos y contenidos.
- **`choques`**: asignaciones con y sin choques, asignaciones vacías y múltiples cursos en la misma aula.
- **`capacidadFallida`**: aulas insuficientes, exactas, mayores y cursos sin asignar.
- **`desperdicio`**: capacidad exacta, cursos sin asignar, aulas con sobra y capacidad insuficiente.
- **`movilidad`**: un solo curso, todos en la misma aula, cursos no asignados y ejemplos del enunciado.
- **`costoAsignacion`**: los dos ejemplos del enunciado y verificación de que choques generan costos mayores a 1000.
- **`generarAsignaciones`**: tamaños correctos, asignaciones de longitud correcta y caso base con 0 cursos.
- **`asignacionOptima`**: costo óptimo no supera referencias conocidas, longitud correcta e índices válidos.
- **`choquesPar`**: mismos casos que la versión secuencial y verificación de que el resultado coincide con `choques`.
- **`desperdicioPar`**: mismos casos que la versión secuencial y verificación de que el resultado coincide con `desperdicio`.
- **`movilidadPar`**: mismos casos que la versión secuencial y verificación de que el resultado coincide con `movilidad`.
- **`generarAsignacionesPar`**: tamaños correctos y verificación de que produce el mismo número de asignaciones que `generarAsignaciones`.
- **`asignacionOptimaPar`**: costo óptimo igual al secuencial, longitud correcta e índices válidos.