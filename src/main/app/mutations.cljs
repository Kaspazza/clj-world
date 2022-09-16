(ns app.mutations
  (:require [com.fulcrologic.fulcro.mutations :refer [defmutation]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [app.ui.categories :refer [CategoryHeader Categories]]
            [app.ui.lesson :as lesson :refer [Lesson]]
            [com.fulcrologic.rad.routing :as routing]
            [applied-science.js-interop :as j]
            [nextjournal.clojure-mode.test-utils :as test-utils]
            [app.code-mirror.sci :as sci]
            [com.fulcrologic.fulcro.components :as comp]
            ["@codemirror/view" :refer [EditorView]]
            ))

(defmutation change-category [{:keys [chosen-id]}]
  (action [{:keys [app state]}]
    (routing/route-to! app Categories {:category-id (name chosen-id)})))

(defmutation load-category-lessons [{:keys [chosen-id]}]
  (action [{:keys [app _state]}]
    (df/load! app [:category/id chosen-id] CategoryHeader {:focus [:category/lessons]})))

(defmutation open-lesson [{:keys [category-id lesson-id]}]
  (action [{:keys [app _state]}]
    (df/load! app [:lesson/id lesson-id] Lesson {:focus [:lesson/id]})
    (routing/route-to! app Lesson {:category-id category-id
                                   :lesson-id lesson-id})))

(defmutation change-active-tab [{:keys [chosen-id]}]
  (action [{:keys [app state]}]
    (let [chosen-id (keyword chosen-id)]
      (doall (map
               (fn [category-id]
                 (if (= chosen-id category-id)
                   (swap! state assoc-in [:category/id category-id :ui/active?] true)
                   (swap! state assoc-in [:category/id category-id :ui/active?] false)))
               (into [] (keys (:category/id @state))))))))

(defmutation update-repl-state [{:keys [lesson-id repl-value evaluated-line]}]
  (action [{:keys [app state]}]
    (swap! state update-in [:lesson/id lesson-id :ui/repl-state] (fn [v]
                                                                     (into [] (conj v {:line (str evaluated-line) :value (str repl-value)}))))))
