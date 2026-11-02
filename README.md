# Project AppleFinder

A Java program that generates a plain text file containing a description 
of the recommended apple variety based on user preferences and reference 
data pulled from public online sources.

## The Problem

There are many varieties of apples, each with different textures, levels of sweetness, etc. 
My dad often asks me which variety tastes better (Pink Lady or Granny Smith?), so I wanted 
to develop a program that searches for a variety that best suits a person’s taste, as 
searching them myself can be tedious. 

## How It Works

A paragraph or a few bullets explaining what the software does at a
high level. If you have a diagram, embed it here:

![System diagram](docs/AppleFinder.pdf)

## Tech Stack

- **Language:** Java 
- **Libraries:** jsoup, Gson
- **External tools:** IntelliJ
- The public data is accessed through Java's HttpClient class
- Sources include (but not limited to) USApple, New York Apple Association, Iowa State University Extension.
- The parsed data will be stored in a JSON file

## Setup

Step-by-step: how does someone who just cloned your repo get it
running?

```bash
git clone https://github.com/you/your-project
cd your-project
./install.sh   # or whatever
```

## Usage

How to actually run the thing, with examples.

```bash
./backup.sh ~/Documents
```

## Roadmap

### MVP scope 

- HttpRequest sources on common US apple varieties and parse data using jsoup/Gson
- Ask for user preferences in terminal; catch exceptions
- Recommend a variety by comparing user preferences with the parsed data
- Generate a .txt file that contains a description of the variety

### Planned Features 

- [ ] Location-based arguments for user preference
- [ ] Best store prices for a variety 
- [ ] Image of apple variety in description
- [ ] Support for CSV data type

## Author

Luke Li, [my GitHub](https://github.com/Luke-jcbts)
