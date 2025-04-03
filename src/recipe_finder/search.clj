(ns recipe-finder.search
  (:require [recipe-finder.tokenise :refer [normalised-tokens]]))

(defn render-recipe
  [recipe-data recipe]
  (->> (get recipe-data recipe)
       (reduce (fn [acc {:keys [section text] :as r}]
                 (assoc acc section text)) {})))

(defn search
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
  [recipe-index recipe-data query]
  (->> (search recipe-index query)
       (map render-recipe)))
