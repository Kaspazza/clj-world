(ns app.mutations
  (:require [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [app.categories :refer [CategoryHeader Categories]]
            [com.fulcrologic.rad.routing :as routing]))

(defmutation change-category [{:keys [chosen-id]}]
  (action [{:keys [app state]}]
    (df/load! app [:category/id chosen-id] CategoryHeader {:focus [:category/content]})
    (doall (map
             (fn [category-id]
               (if (= chosen-id category-id)
                 (swap! state assoc-in [:category/id category-id :ui/active?] true)
                 (swap! state assoc-in [:category/id category-id :ui/active?] false)))
             (into [] (keys (:category/id @state))))))
  (ok-action [{:keys [app]}]
    (routing/route-to! app Categories {:category/id (name chosen-id)})))
