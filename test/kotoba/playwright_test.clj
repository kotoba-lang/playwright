(ns kotoba.playwright-test
  (:require [clojure.test :refer [deftest is]]
            [kotoba.playwright :as pw]))

(deftest availability-returns-boolean
  (is (boolean? (pw/available?))))
