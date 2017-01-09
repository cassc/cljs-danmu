(ns danmu.core
  (:require
   [clojure.string :as s]
   [alandipert.storage-atom :refer [local-storage]]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout alts!]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(defonce dm-items (local-storage (atom []) :dm-items))
(defonce msg-store (atom nil))
(defn add-item []
  (when-not (s/blank? @msg-store)
    (swap! dm-items conj @msg-store)
    (reset! msg-store nil)))

(defn animated-item []
  (let [time (+ 4 (rand-int 20))
        top (rand-nth (range 10 280))
        color (str "rgb(" (rand-int 255) "," (rand-int 255) "," (rand-int 255) ")")]
    (fn [item]
      [:div.animated-item
       {:style
        {:animation-duration (str time "s")
         :top (str top "px")
         :color color
         ;; :animation-delay (str (rand-int 5) "s")
         }}
       item])))

(defn display-dm-items []
  [:div.board
   (for [item @dm-items]
     ^{:key (.random js/Math)}
     [animated-item item])])

(defn my-app []
  (fn []
    [:div.main-app
     [:div
      [display-dm-items]]
     [:div.row
      [:input.msg {:type :text :value @msg-store :on-change #(reset! msg-store (-> % .-target .-value))}]]
     [:div.row
      [:button {:type :button :on-click add-item} "发送"]
      [:button {:type :button} "清屏"]]]))

(defn main []
    (reagent/render [#'my-app] (.getElementById js/document "app")))

(main)

