(ns danmu.core
  (:require
   [reanimated.core :as anim]
   [clojure.string :as s]
   [alandipert.storage-atom :refer [local-storage]]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout alts!]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(defonce dm-items (local-storage (atom []) :dm-items))

(defn gen-animate-item [item]
  {:item item
   :color (str "rgb(" (rand-int 255) "," (rand-int 255) "," (rand-int 255) ")")
   :top (rand-nth (range 10 280))
   :time (+ 5 (rand-int 15))})

(defonce displayed-items (atom (map gen-animate-item @dm-items)))
(defonce msg-store (atom nil))
(defn add-item []
  (when-not (s/blank? @msg-store)
    (swap! dm-items conj @msg-store)
    (reset! msg-store nil)))

(defn- -animated-item []
  (fn [{:keys [item time top color]}]
    [:div.animated-item
     {:style
      {:animation-duration (str time "s")
       :top (str top "px")
       :color color
       ;; :animation-delay (str (rand-int 5) "s")
       }}
     item]))

(defn animated-item []
  (let [cx (atom 0)]
    (fn [{:keys [item time top color]}]
      [:div.animated-item
       [:svg
        {:width 800
         :height 300
         ;;:color color
         }
        [anim/interval #(swap! cx inc) 10]
        [:svg
         [:text {:x @cx :y top} item]]]])))

#_(let [cx (atom 0)
      ;;cx (anim/interpolate-to x)
      ]
  (fn []
    [:svg
     {:width 560
      :height 120}
     [anim/interval #(swap! cx inc) 10]
     [:svg
      [:text {:x @cx :y 100} "hi"]]]))

(defn display-dm-items []
  [:div.board
   (for [aitem @displayed-items]
     ^{:key (.random js/Math)}
     [animated-item aitem])])

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

