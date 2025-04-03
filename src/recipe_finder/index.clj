(ns recipe-finder.index
  (:require
   [clojure.core.reducers :as r]
   [clojure.math :as math]
   [clojure.string :as str]
   [recipe-finder.tokenise :refer [normalised-tokens]]))

(defn- count-document-sections
  [recipe-data]
  (count (apply concat (vals recipe-data))))

(defn- count-term-frequencies
  [recipe-data]
  (reduce (fn [acc [_ recipe]]
            (let [all-text-tokens (frequencies (mapcat normalised-tokens (map :text recipe)))]
              (merge-with + acc all-text-tokens))) {} recipe-data))

(defn- calculate-document-score
  [{:keys [token token-frequency token-count total-recipe-section-count total-term-frequencies section]}]
  (let [section-weights {:title 8 :introduction 2 :ingredients 4 :method 1}
        term-frequency (/ token-frequency token-count)
        document-frequency (get total-term-frequencies token)]
    (* (get section-weights section) term-frequency
       (math/log10 (/ total-recipe-section-count (+ document-frequency 1))))))

(defn make-index
  [recipe-data]
  (let [total-recipe-section-count (count-document-sections recipe-data)
        total-term-frequencies (count-term-frequencies recipe-data)]
    (->> recipe-data
         (r/mapcat (fn [[doc-id entry]]
                     (r/reduce
                      (fn [acc {:keys [text section]}]
                        (let [section-tokens (normalised-tokens text)
                              token-frequencies (frequencies section-tokens)
                              section-token-count (count section-tokens)
                              token-maps (for [token (distinct section-tokens)]
                                           {:token token :doc doc-id :score
                                            (calculate-document-score {:token token
                                                                       :token-frequency (get token-frequencies token)
                                                                       :token-count section-token-count
                                                                       :total-recipe-section-count total-recipe-section-count
                                                                       :total-term-frequencies total-term-frequencies
                                                                       :section section})})]
                          (apply conj acc token-maps)))
                      []
                      entry)))
         (r/reduce (fn [acc {:keys [token doc score]}]
                     (update-in acc [token doc] #(if %
                                                   (+ % score)
                                                   score))) {}))))
