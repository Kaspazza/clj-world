(ns app.ui.lesson
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]))

(defsc Lesson [this {:ui/keys [active?]}]
  {:query [:ui/active?]
   :route-segment ["categories" :category-id :lesson-id]
   :initial-state (fn [_] {:ui/active? true})
   :ident (fn [] [:component/id ::Lesson])}
  (dom/div))
