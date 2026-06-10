# Conclusiones

**Integrantes:** 

Maria Fernanda Betancourt Montoya 

Kevin Andres Rosero Romo



---

## Conclusiones del proyecto

### 1. Programación funcional

Una de las ventajas más grandes fue que al usar funciones como `map`, `filter` y
`flatMap` el código quedó más corto y más fácil de leer que si se hubiera hecho
con ciclos. En lugar de decirle al programa paso a paso cómo recorrer una lista,
simplemente le decimos qué queremos obtener de ella.

La dificultad principal fue cambiar la forma de pensar. Al principio el instinto
era usar un ciclo `for` o una variable que se va actualizando, pero las reglas del
proyecto no lo permitían. Entender `flatMap` fue lo que más costó, especialmente
en `generarAsignaciones`, donde cada llamada recursiva construye sobre los resultados
de la anterior hasta tener todas las combinaciones posibles.

### 2. Corrección

Para verificar que las funciones estaban bien implementadas las comparamos directamente
con las definiciones matemáticas del enunciado. Por ejemplo, `solapan` implementa
exactamente la condición $\text{ini}_1 < \text{fin}_2 \land \text{ini}_2 < \text{fin}_1$,
y `choques` garantiza que cada par de cursos se revisa una sola vez usando $j > i$.

Para `generarAsignaciones` se argumentó por inducción: con $k = 0$ hay exactamente
una asignación vacía, y con cada paso se agregan todas las aulas posibles a cada
asignación anterior, lo que garantiza que al final se generan exactamente $m^k$
asignaciones.

### 3. Paralelismo

El paralelismo ayudó cuando las entradas eran grandes, con $n \geq 6$ cursos y
$m \geq 4$ aulas, porque había suficiente trabajo para repartir entre los dos hilos
y la ganancia superaba el costo de coordinarlos.

Con entradas pequeñas como $n \leq 4$ y $m \leq 3$ las versiones paralelas fueron
más lentas que las secuenciales. El problema es que crear y sincronizar hilos tiene
un costo fijo, y si el trabajo a realizar es poco, ese costo no vale la pena.

### 4. Aprendizajes

Lo más útil del curso fue entender las funciones de alto orden y la recursión.
`flatMap` en particular fue clave para construir el espacio de asignaciones, y
la recursión de `generarAsignaciones` fue un buen ejemplo de cómo dividir un
problema grande en casos más pequeños.

Si volviéramos a empezar intentaríamos descartar asignaciones parciales que ya
tienen un costo muy alto antes de seguir construyéndolas, lo que haría el programa
más rápido para entradas grandes.