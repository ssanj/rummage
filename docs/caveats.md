# Caveats

Due to time constraints I have not been able to complete this assignment to my full satisfaction. I have two young kids and work full-time - as such I have limited free time to work on an assignment of this size. I hope my solution has covered all, if not most, bases as is.

I've noted below some of the things I would have liked to have completed but didn't get time to.

## More Tests

While I do have unit tests (example-based and property-based) I wish I had time to test more things. I would specially have liked to have written tests around the cli portion of the solution.

## Typeclass Laws

Typeclasses should have laws so you have some guidelines on how to implement them. I didn't have time to come up with rigorous laws surrounding the typeclasses I created.

## Constraining how FieldValues are Created

`zendesk.rummage.model.FieldValue` should ideally be created only via the `zendesk.rummage.model.domain.typeclass.ToFieldValue` typeclass. I didn't have time to investigate how to constrain this construction.


### Deriving Encoders/Decoders from the DataMapper

The `zendesk.rummage.model.domain.DataMapper` has a lot of information that could be used to generate JSON Encoders/Decoders from one source of truth. At the moment the JSON field names are specified in the Encoder/Decoder and in the field mappings passed to `DataMapper`. While I can see an easy way to generate the Encoder (domain object to JSON conversion) from the `DataMapper` there doesn't seem to be a very easy way to write the Decoder (JSON to domain object conversion).

The main issue being mapping from some collection of field names and values to the constructor of the domain object. This can be done with tools like [Shapeless](https://github.com/milessabin/shapeless) but as indicated previously under [scala design choices](scala-design-choices.md#shapeless) that would add unnecessary complexity to this solution. As such I have left some of this duplication in. Given that all the fields are mentioned in the same location - the domain companion object; it should be pretty easy to see all the duplication points and update them all as necessary.

Had I had more time I would have liked to have solved this problem of deriving the Encoders and Decoders directly from just defining the `DataMapper` instance.
