# Data Quality

## Optionality

Given there is no JSON schema (or other schema) for this data and the limited data set supplied with the problem, any of these fields may be optional (missing, null etc.). I have chosen to use the `_id` field as mandatory - given there's no point searching across an input if we can't uniquely identify it. Therefore all other fields have been made optional to prevent decoding errors and make sparse data sets usable for searching.

## Counting the number of fields per input file

The number of fields per array index was counted to ensure we encompassed all possible fields.

### User

#### Fields

Ran the following `jq` query:

```
cat users.json | jq '.[] | length' | sort | uniq
```

which resulted in:

```
3
4
```

This meant some records for user are missing a single field.

I ran the following query to identify which field was missing:


```
cat users.json | jq '.[] | (length|tostring) + " " + (._id|tostring)'
```

which resulted in:


```
"4 1"
"4 2"
"4 3"
"4 4"
"4 5"
"4 6"
"4 7"
"4 8"
"4 9"
"4 10"
"4 11"
"4 12"
"4 13"
"4 14"
"4 15"
"4 16"
"4 17"
"4 18"
"4 19"
"4 20"
"4 21"
"4 22"
"4 23"
"4 24"
"4 25"
"4 26"
"4 27"
"4 28"
"4 29"
"4 30"
"4 31"
"4 32"
"4 33"
"4 34"
"4 35"
"4 36"
"4 37"
"4 38"
"4 39"
"4 40"
"4 41"
"4 42"
"4 43"
"4 44"
"4 45"
"4 46"
"4 47"
"4 48"
"4 49"
"4 50"
"4 51"
"4 52"
"4 53"
"3 54" //missing field
"3 55" //missing field
"4 56"
"4 57"
"4 58"
"4 59"
"4 60"
"4 61"
"4 62"
"4 63"
"4 64"
"4 65"
"4 66"
"4 67"
"4 68"
"4 69"
"4 70"
"4 71"
"4 72"
"4 73"
"4 74"
"4 75"
```

When I had a look at records `54` and `55` I noticed they had missing `verified` fields:

```json
[
  {
    "_id": 54,
    "name": "Spence Tate",
    "created_at": "2016-01-03T02:38:58-11:00"
  },
  {
    "_id": 55,
    "name": "Thelma Wong",
    "created_at": "2016-04-24T03:09:27-10:00"
  },
]
```

I used the following 4 fields to model a User:

```
_id
name
created_at
verified
```

## Ticket


#### Fields


Ran the following `jq` query:

```
cat tickets.json | jq '.[] | length' | sort | uniq
```

which resulted in:

```
5
6
```

This meant some records for ticket are missing a single field.

I then ran the following query to identify which field was missing:

```
cat tickets.json | jq '.[] | (length|tostring) + " " + (._id|tostring)'
```

which resulted in (other matched redacted):

```
"5 c68cb7d7-b517-4d0b-a826-9605423e78c2"
```

When I had a look at record `c68cb7d7-b517-4d0b-a826-9605423e78c2` I noticed it had missing `assignee_id` field:

```json
{
  "_id": "c68cb7d7-b517-4d0b-a826-9605423e78c2",
  "created_at": "2016-03-09T01:39:48-11:00",
  "type": "task",
  "subject": "A Problem in Western Sahara",
  "tags": [
    "Massachusetts",
    "New York",
    "Minnesota",
    "New Jersey"
  ]
}
```


I used the following 6 fields to model a Ticket:

```
_id
created_at
type
subject
assignee_id
tags
```

#### Ticket Type

I found the different values for ticket type with:

```
cat tickets.json | jq '.[] | .type' | sort | uniq
```

which resulted in:

```
"incident"
"problem"
"question"
"task"
null
```

While I could have modelled this as an ADT, because we are only doing an exact text search there doesn't seem to be much point in doing that.


#### assignee id

As mentioned assignee_id is optional and can be missing from the ticket payload. It is assumed that the assignee_id maps to the User id from the users.json input file. This seems to hold true given the examples provided in the assignment.
