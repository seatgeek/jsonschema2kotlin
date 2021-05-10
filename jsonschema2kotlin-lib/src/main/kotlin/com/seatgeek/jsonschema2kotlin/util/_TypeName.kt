import com.squareup.kotlinpoet.TypeName

fun TypeName.nullable(nullable: Boolean = true): TypeName {
    return this.copy(nullable = nullable)
}