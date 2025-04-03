(ns recipe-finder.search
  (:require [recipe-finder.tokenise :refer [normalised-tokens]]))

(defn render-recipe
  "Given a map of recipes and a recipe id, return the data for the recipe:
  {:title title-goes-here :introduction intro-goes-here
   :ingredients ingredients-go-here :method method-goes-here}}"
  [recipe-data recipe]
  (->> (get recipe-data recipe)
       (reduce (fn [acc {:keys [section text] :as r}]
                 (assoc acc section text)) {})))

(defn search
  "Split a search query into separate words, look them up in the index,
  then combine them and sort by relevance score, returning the first 10 results.
  Returns a vector of recipe ids"
  [recipe-index query]
  (let [tokens (normalised-tokens query)
        docs (map #(get recipe-index %) tokens)
        merged (apply merge-with + docs)]
    (->> merged
         (seq)
         (sort-by second >)
         (take 10)
         (map first))))

(defn get-recipes
  "Search for recipes, then map over the ids to return a vector of the  full recipe data"
  [recipe-index recipe-data query]
  (->> (search recipe-index query)
       (map (partial render-recipe recipe-data))))
