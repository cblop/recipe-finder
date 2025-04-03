(ns recipe-finder.repl
  (:require
   [recipe-finder.index :refer [make-index]]
   [recipe-finder.read-files :refer [get-recipe-data]]
   [recipe-finder.search :refer [search get-recipes]]))

(def recipe-data (get-recipe-data "resources/recipes"))
(def recipe-index (make-index recipe-data))

(time
 (search recipe-index "broccoli stilton soup"))

(time
 (search recipe-index "garlic potatoes"))
