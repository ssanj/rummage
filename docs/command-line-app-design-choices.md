# Command line App Design Choices

## Data Input

The input data JSON files are sourced from a data directory instead of loading each file individually through command line arguments. This keeps things simple in that all data can be read from one location and as long as the file names match (`users.json`, `tickets.json`) - everything just works. This allows us to curate "data sets" of input files that we know go together.

## Input Field Display

Instead of having a separate option to display the field names of an input data source, the field names are always displayed. This is more user friendly as you need to know the names of the fields *when* you are about to do a search.

## Programs

The command line application is design through a series of programs that each handle a little part of the user interface. This helps with separation of concerns and allows us to add more functionality to the application easily (by just adding more programs).
