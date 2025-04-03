(ns recipe-finder.tokenise
  (:require
   [clojure.string :as str]))

(defn- tokenise
  "Returns a vector of tokens, split on non-alphanumeric characters"
  [in-string]
  (str/split in-string #"\W+"))

(defn- remove-punctuation-and-numbers
  [in-string]
  (str/replace in-string #"[^a-z\s]" ""))

(defn- make-lower-case
  [in-string]
  (str/lower-case in-string))

(defn- remove-stop-words
  "Removes unneeded commonly used English words"
  [tokens]
  (let [stop-words #{"a" "and" "be" "have" "i" "in" "of" "that" "the"
                     "to" "with" "for" "or" "is" "it" "you" "on" "if" "then" "s" "why" "y"}]
    (remove stop-words tokens)))

(defn normalised-tokens
  "Tokenise and normalise strings"
  [entry]
  (->> entry
       (make-lower-case)
       (remove-punctuation-and-numbers)
       (tokenise)
       (remove-stop-words)))
