# data-dir

Add your sample files for `users.json` and `tickets.json` in this directory.

Then run the application specifying this directory as the data directory

With SBT:

```
sbt 'runMain zendesk.rummage.Rummage data-dir'
```

From within SBT console:

```
runMain zendesk.rummage.Rummage data-dir
```

With Docker:

```
auto/run data-dir
```
