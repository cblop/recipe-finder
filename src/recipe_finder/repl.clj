(ns recipe-finder.repl
  (:require
   [recipe-finder.index :refer [make-index]]
   [recipe-finder.read-files :refer [get-recipe-data]]
   [recipe-finder.search :refer [search get-recipes]]))

;; Open this file in your REPL and evaluate line-by-line

;; First we read the recipe data files and create a map of ids to recipe data
(def recipe-data (get-recipe-data "resources/recipes"))

;; Then we use this to create an inverted index mapping tokens
;; to documents with relevance scores (should take about 4 seconds)
(def recipe-index (make-index recipe-data))

;; Then we can feed this into the search function to return a vector of recipe ids
;; (time taken is printed to stdout for your convenience)
(time
 (search recipe-index "broccoli stilton soup"))

(time
 (search recipe-index "cabbage"))

;; To get the full recipe data, call get-recipes with the index and data instead:
(time
 (get-recipes recipe-index recipe-data "broccoli stilton soup"))

(time
 (get-recipes recipe-index recipe-data "cabbage"))
