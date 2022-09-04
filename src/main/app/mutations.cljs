(ns app.mutations
  (:require [com.fulcrologic.fulcro.mutations :refer [defmutation]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [app.ui.categories :refer [CategoryHeader Categories]]
            [app.ui.lesson :refer [Lesson]]
            [com.fulcrologic.rad.routing :as routing]))

(defmutation change-category [{:keys [chosen-id]}]
  (action [{:keys [app state]}]
    (routing/route-to! app Categories {:category-id (name chosen-id)})))

(defmutation load-category-lessons [{:keys [chosen-id]}]
  (action [{:keys [app _state]}]
    (df/load! app [:category/id chosen-id] CategoryHeader {:focus [:category/content]})))

(defmutation open-lesson [{:keys [category-id content-id]}]
  (action [{:keys [app _state]}]
    (prn "params: " {:category-id category-id
                     :content-id content-id})
    (df/load! app [:content/id content-id] Lesson {:focus [:content/id]})
    (routing/route-to! app Lesson {:category-id category-id
                                   :content-id content-id})))

(defmutation change-active-tab [{:keys [chosen-id]}]
  (action [{:keys [app state]}]
    (let [chosen-id (keyword chosen-id)]
      (doall (map
               (fn [category-id]
                 (if (= chosen-id category-id)
                   (swap! state assoc-in [:category/id category-id :ui/active?] true)
                   (swap! state assoc-in [:category/id category-id :ui/active?] false)))
               (into [] (keys (:category/id @state))))))))

(defmutation update-repl-state [{:keys [content-id repl-value evaluated-line]}]
  (action [{:keys [app state]}]
    (swap! state update-in [:content/id content-id :ui/repl-state] (fn [v]
                                                                     (into [] (conj v {:line (str evaluated-line) :value (str repl-value)}))))))