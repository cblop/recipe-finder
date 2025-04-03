# recipe-finder

A Clojure project for searching through recipes

## Setup

The recipe data isn't included in this repo, so please extract all the recipe text files to this directory:

`/resources/recipes`

## Usage

Rather than running from the command line and waiting for the JVM to start up each time, I've designed this code to be run from the REPL.

Open up `/src/recipe_finder/repl.clj` and evaluate the code one line at a time to run some examples

## Namespaces

### read-files
Use `read-files/get-recipe-data` to read all files from the `/resources/recipes` directory into a map in this format:

``` clojure
{"potato-and-leek-pie" [{:section :title :text "Potato and leek pie"}
                        {:section :introduction "A delicious pie"}
                        {:section :ingredients :text "Potatoes and leeks"}
                        {:section :method :text "Cook the potatoes and leeks"}]
 "cottage-pie"         [{:section :title :text "Cottage pie"}
                        {:section :introduction "A delicious pie"}
                        {:section :ingredients :text "Beef and potatoes"}
                        {:section :method :text "Cook the beef and potatoes"}]}
```

### tokenise
`tokenise/normalised-tokens` converts strings into vectors of tokens, and normalises by converting to lowercase and removing non-alphabetical characters

``` clojure
"A delicious potato pie for 3!" -> ["delicious" "potato" "pie"]
```

### index
`index/make-index` creates an inverted index used to find the documents a token (search term) appears in, and how relevant each document is.

The relevance is calculated using Term Frequency-Inverse Document Frequency (TF-IDF), multiplied by section weights that prioritise the recipe title and ingredients. (see `index/calculate-document-score` in this namespace).

The inverted index has this format, with search terms as keys mapping to maps with document-ids as keys and relevance scores as values:

``` clojure
{"potato" {"leek-and-potato-soup" 1.5
           "potato-quiche" 1.2}
 "broccoli" {"broccoli-stilton-soup" 1.5}}
```
### search
The `search/search` function returns a vector of document ids. These can then be passed to `search/render-recipe` to return the full text of a recipe in this format:

``` clojure
{:title "Potato and leek pie"
 :introduction "A delicious pie"
 :ingredients "Potatoes and leeks"
 :method "Cook the potatoes and leeks"}
```

 `search/get-recipes` is a convenience function to search for a recipe then render its content.
