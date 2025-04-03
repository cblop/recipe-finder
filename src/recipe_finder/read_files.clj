(ns recipe-finder.read-files
  (:require
   [clojure.core.reducers :as r]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn- split-recipe-sections
  "Given a single recipe, parse the title, introduction, ingredients and method sections.
  Return a vector of section maps in the format:
  [{:section :title :text \"Title goes here\"}
   {:section :method :text \"Method goes here\"]
  A vector is used rather than a map for ease of processing when indexing,
  and so we don't have to look up a fixed set of keys later"
  [recipe]
  (let [[_ title introduction ingredients method]
        (re-matches #"^(.+)\n(?:Introduction:\n(.*)\n?)*(?:Ingredients:\n(.*)\n?)*(?:Method:\n(.*))*" recipe)]
    (cond-> []
      (not-empty title) (conj {:text title :section :title})
      (not-empty introduction) (conj {:text introduction :section :introduction})
      (not-empty ingredients) (conj {:text ingredients :section :ingredients})
      (not-empty method) (conj {:text method :section :method}))))

(defn get-recipe-data
  "Reads all .txt files in the path and returns a map in this format:
  {filename-1: [{:section :introduction :text \"intro text\"} {:section :ingredients :text \"ingredients go here\"}]
   filename-2: [... sections as above]}"
  [path]
  (let [recipe-dir (io/file path)
        recipe-files (.list recipe-dir)]
    (r/reduce (fn [acc f]
                (assoc acc
                       (str/replace f ".txt" "")
                       (split-recipe-sections (slurp (str path "/" f))))) {} recipe-files)))
