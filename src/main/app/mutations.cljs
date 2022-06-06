(ns app.mutations
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(defmutation set-active-category [{:keys [chosen-id]}]
  (action [{:keys [state]}]
    (doall (map
             (fn [category-id]
               (if (= chosen-id category-id)
                 (swap! state assoc-in [:category/id category-id :ui/active?] true)
                 (swap! state assoc-in [:category/id category-id :ui/active?] false)))
             (into [] (keys (:category/id @state)))))))
