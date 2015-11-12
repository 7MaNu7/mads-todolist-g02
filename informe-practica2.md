** Héctor José Compañ Gabucio **

# Informe práctica 2. TDD

## Ventajas
TDD nos "obliga" a implementar test al código que hagamos, y esto se agradece ya que **mejora la calidad del código**.

También nos hace pensar **qué es lo que hay que hacer antes de hacerlo**, y esto ayuda a planificar cómo será el código y qué objetivos tiene que cumplir para pasar los test. Si lo vemos desde el punto de vista del cliente, TDD nos ayudará a **resolver los problemas del cliente**, ni más ni menos, y esto al fin y al cabo nos ahorrará trabajo.

También he notado que he tenido que hacer **menos debugging en busca de errores**, supongo que es porque al tener tantos test puedes saber rápidamente qué parte del código te está fallando.

## Desventajas
Hablemos ahora de las desventajas de TDD. Yo por lo menos he notado que, si bien la lógica de negocio y servicio son de gran calidad, **la capa de presentación no recibe tanta calidad**. Es decir, la interfaz puede ser muy sencilla para poder pasar un test, pero queda con muy poca calidad.

Otra desventaja es el **control de la BD de pruebas**. Al tener tantos test es necesario una BD de pruebas, y a simple vista esta BD no da problemas. Pero si se te olvida cerrarla, o si los id se autoincrementan, o si al empezar los test no estaba en un estado inicial X, o si actualizas la BD pero el contenido anterior estaba en memoria...Diría que los problemas más extraños los tuve con la BD de pruebas.

También he notado que **mi productividad ha bajado**. Si bien es verdad que la calidad del código ha mejorado, habré conseguido hacer 2-3 features en el tiempo que hice 6 o más features para la práctica anterior. Esto es debido a que adoptar TDD es un proceso tedioso: Montar la BD, preparar sus estados iniciales, hacer una clase test por cada feature a probar, hacer un método para cada salida posible...

## Conclusión
Si estás buscando un enfoque que te ayude a mejorar la calidad de tu código de una forma ágil, sin toneladas de documentación o diseño, TDD es tu amigo. Siempre y cuando puedas permitirte un descenso en la productividad en las primeras semanas.
