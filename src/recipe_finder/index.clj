(ns recipe-finder.index
  (:require
   [clojure.core.reducers :as r]
   [clojure.math :as math]
   [clojure.string :as str]
   [recipe-finder.tokenise :refer [normalised-tokens]]))

(defn- count-document-sections
  "Returns count of the total number of sections (title, intro, method, etc)
  over all recipe documents, for use when calculating document relevance"
  [recipe-data]
  (count (apply concat (vals recipe-data))))

(defn- count-term-frequencies
  "Returns a map of the total number of token counts over all recipe data, e.g:
  {potato 802
   broccoli 999}"
  [recipe-data]
  (reduce (fn [acc [_ recipe]]
            (let [all-text-tokens (frequencies (mapcat normalised-tokens (map :text recipe)))]
              (merge-with + acc all-text-tokens))) {} recipe-data))

(defn- calculate-document-score
  "Returns a number indicating the relevancy of a document to a particular token.
  Uses term frequency–inverse document frequency (TF-IDF), multiplied by weights
  for each recipe section, so that tokens in titles or ingredients score higher"
  [{:keys [token token-frequency token-count total-recipe-section-count total-term-frequencies section]}]
  (let [section-weights {:title 8 :introduction 2 :ingredients 4 :method 1}
        term-frequency (/ token-frequency token-count)
        document-frequency (get total-term-frequencies token)]
    (* (get section-weights section) term-frequency
       (math/log10 (/ total-recipe-section-count (+ document-frequency 1))))))

(defn make-index
  "Returns an inverted index: a map where the keys are the tokens
  and the values are maps of document keys with relevance score values:
  {potato {leek-and-potato-soup 1.5
           potato-quiche 1.2}
  broccoli {broccoli-stilton-soup 0.8}]"
  [recipe-data]
  (let [total-recipe-section-count (count-document-sections recipe-data)
        total-term-frequencies (count-term-frequencies recipe-data)]
    (->> recipe-data
         ;; first create a vector of {:token token :doc doc-id :score relevance-score} for all tokens
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
         ;; then combine them into the inverted index with tokens as kes and values as {document-id score}
         (r/reduce (fn [acc {:keys [token doc score]}]
                     (update-in acc [token doc] #(if %
                                                   (+ % score)
                                                   score))) {}))))
