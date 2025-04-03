(ns recipe-finder.read-files
  (:require
   [clojure.core.reducers :as r]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn- split-recipe-sections
  [recipe]
  (let [[_ title introduction ingredients method]
        (re-matches #"^(.+)\n(?:Introduction:\n(.*)\n?)*(?:Ingredients:\n(.*)\n?)*(?:Method:\n(.*))*" recipe)]
    (cond-> []
      (not-empty title) (conj {:text title :section :title})
      (not-empty introduction) (conj {:text introduction :section :introduction})
      (not-empty ingredients) (conj {:text ingredients :section :ingredients})
      (not-empty method) (conj {:text method :section :method}))))

(defn get-recipe-data
  [path]
  (let [recipe-dir (io/file path)
        recipe-files (.list recipe-dir)]
    (r/reduce (fn [acc f]
                (assoc acc
                       (str/replace f ".txt" "")
                       (split-recipe-sections (slurp (str path "/" f))))) {} recipe-files)))
