# Scala Design Choices

## SBT Compiler flags

The compiler flags specified in the `build.sbt` are based on [tpolecat's sbt compiler flag recommendations](https://tpolecat.github.io/2017/04/25/scalac-flags.html). They were [updated by tabdulradi for Scala 2.13](https://gist.github.com/tabdulradi/aa7450921756cd22db6d278100b2dac8). I also fixed issues with `nullary-override` and  `-Wself-implicit`.

The reason for using these compiler flags is to increase code quality. They catch many bugs straight off the bat without having
to write a single test! Strong type are one of Scala's super powers!


## Wartremover

[Wartremover](https://www.wartremover.org/) is a compile-time linter that also catches many unsafe practices during compilation. A list of built-in warts can be found [here](https://www.wartremover.org/doc/warts.html). I have [turned on all warts](../wartremover.sbt) as compiler errors. It is configured as an SBT plugin through `project/plugins.sbt`.


## Value Classes

Used [Value Classes](https://docs.scala-lang.org/overviews/core/value-classes.html) to negate allocations for wrapper types at runtime. Given the large number of objects that would be in memory for the search indexes etc., I chose to use this optimization.

For example:

```scala
class Wrapper(val underlying: Int) extends AnyVal
```

is represented as an `Int` at runtime and a `Wrapper` at compile-time, giving us compile-time guarantees without the runtime cost!

But they have to be used in specific ways to achieve this. The following needs to be avoided:

1. A value class being treated as another type.
1. A value class being assigned to an array.
1. Doing runtime type tests, such as pattern matching.

It has the following limitations:

1. It must have only a primary constructor with exactly one public, val parameter whose type is not a user-defined value class
1. It may not have @specialized type parameters.
1. It may not have nested or local classes, traits, or objects
1. It may not define concrete equals or hashCode methods.
1. It must be a top-level class or a member of a statically accessible object
1. It can only have defs as members. In particular, it cannot have lazy vals, vars, or vals as members.
1. It cannot be extended by another class.

More details can be found [here](https://docs.scala-lang.org/overviews/core/value-classes.html)

A benefit of wrapper types is that it allows for:
- Easier creation of [Property-Base Testing](https://fsharpforfunandprofit.com/pbt/) [Generators](https://github.com/typelevel/scalacheck/blob/main/doc/UserGuide.md) (aids in testing)
- Prevents mixing up fields with the same types (reduces errors through stronger types)
- Encourages use of typeclasses to model behaviour, as we have distinct types

## General guidelines

- Separated side effecting code from pure code)
- Didn't throw Exceptions, used Option or Either
- Didn't use nulls, used Option or Either
- Didn't mutate variables
- Modeled side effects as descriptions of actions (effects) that would be "run" through interpretation

## Algebraic Data Types (ADTs)

Used [ADT](https://alvinalexander.com/scala/fp-book/algebraic-data-types-adts-in-scala/)s to make invalid states unrepresentable.

An example of an ADT is `zendesk.rummage.model.TableType`

```scala
sealed trait TableType
object TableType {
  case object UserTableType         extends TableType
  case object TicketTableType       extends TableType
  ...
}
```

## NonEmpty

Used `cats.data.NonEmptyVector` to represent the fieldMappings in `zendesk.rummage.model.domain.typeclass.DataMapper` typeclass to prevent the creation of empty mappings. An empty mapping would be meaningless.

```scala
  mappings: NonEmptyVector[DomainFieldMapping[Domain]]
```

## Manual mapping of JSON Fields to Values

### Using Encoder/Decoder .forProductN

I've used the `Decoder.forProductN` and `Encoder.forProductN` variation when mapping between the field names from the JSON input and domain objects. While there are more advanced techniques to do this, I've chosen to go with this option because:
- We have a small number of fields which makes the labour acceptable.
- We need to decode a large number of these domain objects so we don't want something expensive or error-prone.
- We want to keep things simple.

From the [Circe Docs](https://circe.github.io/circe/codecs/custom-codecs.html):

> It’s worth noting that if you don’t want to use the experimental generic-extras module, the completely unmagical
> forProductN version isn’t really that much of a burden
> While this version does involve a bit of boilerplate, it only requires circe-core, and may have slightly better
> runtime performance in some cases.

### Other Options Considered

#### Macros

[Macro](https://docs.scala-lang.org/overviews/macros/overview.html) code in Scala doesn't port well, with the macro API breaking between most major Scala versions. It would also require the reviewer to understand the Scala AST to review the solution.

#### Shapeless

[Shapeless](https://github.com/milessabin/shapeless) is:
> a type class and dependent type based generic programming library for Scala

It has many tools to slice and dice case class into their constituent elements which would have helped in having an automatic derivation of the encoder/decoders for domain objects. Shapeless is also quite specific and has a learning curve which is high and seems unnecessary for this solution.

#### Reflection

Scala [reflection](https://docs.scala-lang.org/overviews/reflection/overview.html) does provide an easy way to introspect case classes for fields and their values. Unfortunately you loose a lot of type information. It's slow for something that could be used frequently.

A simple example of reflection to retrieve a case classes' fields:

```scala
import scala.reflect.runtime.universe._

def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
  case m: MethodSymbol if m.isCaseAccessor => m
}.toList
```

Scala 2.13 also has some useful `product` functions on [Product](https://www.scala-lang.org/api/2.13.x/scala/Product.html) which all case classes extend. Unfortunately they all return the [Any](https://www.scala-lang.org/api/2.13.x/scala/Any.html) as the type and are not very type safe (as Any could represent any type in the Scala type system) and would require runtime casting etc., which doesn't give us compile-time guarantees.

```scala
//example product functions
def productElement(n: Int): Any
def productIterator: Iterator[Any]
```


## Tagless Final Style

I used the [Tagless Final](https://blog.softwaremill.com/final-tagless-seen-alive-79a8d884691d) style to implement the solution. The other options considered were `records of functions` or ([Scrap your typeclasses](https://www.haskellforall.com/2012/05/scrap-your-type-classes.html)) and inheritance.


## Package Structure

The main packages at the top level are:

 |Package | Purpose |
 | ------ | ------- |
 | root   | main app |
 | algebra | All behaviour definitions and implementations |
 | model   | All data models |
 | cli     | Everything related to the cli client. This has been separated out because there could be many other clients and they could each have their own packages |


### Algebra

The `algebra` package is further broken down.


 |Package | Purpose |
 | ------ | ------- |
 | algebra.search | All the searching related behaviours |
 | algebra.search.table | All the searching related behaviours using a "table" concept |


### Model

The `model` package is further broken down.

 |Package | Purpose |
 | ------ | ------- |
 | root | Common models used through the solution |
 | domain | The domain types |
 | domain.typeclasses | Typeclasses pertaining to domain types |
 | domain.field | Wrapper types used for fields in domain types |
 | domain.search| Types used for search functionality |

### Cli

The `cli` package is further broken down.

 |Package | Purpose |
 | ------ | ------- |
 | root   | Cli app |
 | cli.algebra | The behaviours for the cli |
 | cli.model   | The models for the cli |


## Implicit Classes

### DataMapper

Class: `zendesk.rummage.model.domain.DataMapper`

The `DataMapper` describes how domain fields are mapped to JSON input fields. It needs two polymorphic types `PK` and `Domain` which represent the primary key type for the domain type and the domain type respectively.

A `DataMapper` instance for can be created through the  `DataMapper.from` function :

```scala
def from[PK, Domain](mappings: NonEmptyVector[DomainFieldMapping[Domain]], pk: Domain => PK): DataMapper[PK, Domain]
```

The `mappings` parameter specifies how JSON fields are mapped to domain fields along with how to retrieve the value of that field given a domain object. The `pk` function defines how to retrieve a primary key for the give domain type.

A `zendesk.rummage.model.DomainFieldMapping` is defined as:

```scala
final case class DomainFieldMapping[Domain](val jsonField: JsonFieldName, val domainField: FieldName, val getter: Domain => FieldValue)`
```

It represents a mapping between a JSON field name, a domain field name and the corresponding getter for the value of the domain field.

An example of a `DataMapper` for `User`:

```scala
  private val fieldMappings: NonEmptyVector[DomainFieldMapping[User]] = {
    NonEmptyVector.of[DomainFieldMapping[User]](
      DomainFieldMapping(
        JsonFieldName("_id"), FieldName("id"), value => ToFieldValue[UserId].toFieldValue(value.id)),
       DomainFieldMapping(
        JsonFieldName("name"), FieldName("name"), value => ToFieldValue[Option[Name]].toFieldValue(value.name)),
      DomainFieldMapping(
        JsonFieldName("created_at"), FieldName("createdAt"), value => ToFieldValue[Option[Timestamp]].toFieldValue(value.createdAt)),
      DomainFieldMapping(
        JsonFieldName("verified"), FieldName("verified"), value => ToFieldValue[Option[Verified]].toFieldValue(value.verified))
    )
  }

  implicit val dataMapperUser: DataMapper[UserId, User] = DataMapper.from[UserId, User](fieldMappings, _.id)
```

The `DataMapper` instance is exposed as an implicit value.

Any `DataMapper` instance can be summoned using the `apply` function on the `DataMapper` companion object:

```scala
def apply[PK, Domain](implicit dm: DataMapper[PK, Domain]): DataMapper[PK, Domain]
```

The reason we need this mapping is that the JSON field names and the domain field names can be different. There are some reasons why we would want this:

- The JSON input fields might be reserved words in Scala (eg. `type` field in the `tickets.json`)
- We want to use idiomatic field naming in our Scala objects (camel case) separately to how the JSON fields are named (snake case)
- We don't want to be bound to JSON field names - if JSON field name changes we don't necessarily want to change the Scala domain field name as well.

See [ToFieldValue](#tofieldvalue) for how the field values are stored in the `DomainFieldMapping`.

## Custom Typeclasses

From the Cats [docs](https://typelevel.org/cats/typeclasses.html)

> Type classes are a powerful tool used in functional programming to enable ad-hoc polymorphism, more commonly known as overloading. Where many object-oriented languages leverage subtyping for polymorphic code, functional programming tends towards a combination of parametric polymorphism (think type parameters, like Java generics) and ad-hoc polymorphism.

A blog post on typeclasses can be found [here](https://nrinaudo.github.io/scala-best-practices/definitions/type_class.html)


### ToFieldValue


Typeclass: `zendesk.rummage.model.domain.typeclass.ToFieldValue`

The `ToFieldValue` typeclass, specifies how a type is converted to a domain field value.  The `ToFieldValue` typeclass takes a single polymorphic type `T` which represent the type that can be converted to a `zendesk.rummage.model.FieldValue`.

It is defined as:

```scala
trait ToFieldValue[T] {
  def toFieldValue(value: T): FieldValue

  def defaultValue: FieldValue = FieldValue("")
}
```

The `defaultValue` specifies how to present the type `T` as a `FieldValue` should an instance not exist or is "empty". We could potentially have different values for different types here but for our use case we've gone with an empty `String`.

An example implementation of `ToFieldValue` for `TicketId` is:

```scala
implicit val ticketIdToFieldValue: ToFieldValue[TicketId] = ToFieldValue[String].contramap(_.value)
```

which is based on the `String` implementation for `ToFieldValue`:

```scala
  implicit val stringToFieldValue: ToFieldValue[String] = new ToFieldValue[String] {
    override def toFieldValue(value: String): FieldValue = FieldValue(value)
  }
```

We use a typeclass here for the following reasons:

- Ensure consistent creation of domain field values. There's one way to get this for any supported type.
- Automatically derive instances for containers like `Option` and `Seq`.
- Reusing existing `ToFieldValue` instance when generating instances for wrapper types using the `contramap` function.

Ideally we should be only able to create `FieldValue`s through this mechanism, but that is not the case. If I had more time I would have investigated how to constrain this more.


## Keeping values as Strings

Because we need to perform exact text matches against the input data as per the requirements, I've encoded each value as a `zendesk.rummage.model.FieldValue` - which is essentially a wrapper class around a `String`. While some of the input data like the `type` field of the ticket data could have been modelled as ADTs, there is little point since we need to do exact text comparisons. If this changes in the future `FieldValue` can be encoded to be an ADT with the required states.


