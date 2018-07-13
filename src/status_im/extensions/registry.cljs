(ns status-im.extensions.registry
  (:require [pluto.reader :as reader]
            [pluto.registry :as registry]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.ui.components.react :as react]))

(def components
  {'view react/view
   'text react/text})

(def extensions
  '{meta {:name          ""
          :description   ""
          :documentation ""}

    views/STRK
    [view {}
     [view {:style {:flex 1}}
      [text {}
       "STRK"
       #_(or name (i18n/label :t/cryptokitty-name {:id id}))]
      [text {}
       "Short bio"]]]

    hooks/status.collectibles.STRK
    {:name     "CryptoStrikers"
     :symbol   :STRK
     :view     @views/STRK
     :contract "0xdcaad9fd9a74144d226dbf94ce6162ca9f09ed7e"}

    views/command.send.token.selector
    [list/item {}
     [text ""]]

    views/send.preview
    [view {}
     [text {}
      "Command Preview"]]

    views/send.short-preview
    [view {}
     [text {}
      "Short Preview"]]

    hooks/status.chat.commands.test-command
    {:description   "Test command"
     :scopes        [{:scope :personal-chats}]
     :preview       @views/send.preview
     :short-preview @views/send.short-preview
     :parameters    [{:id          :asset
                      :type        :text
                      :placeholder "Currency"}
                     {:id          :amount
                      :type        :number
                      :placeholder "Amount"}]}})

(def capacities
  {:components components
   :events     [{:name 'events/status.wallet.send}]
   :hooks      {'hooks/status.collectibles {:properties {:name     :string
                                                         :symbol   :keyword
                                                         :view     :view
                                                         :contract :string}}
                'hooks/status.chat.commands {:properties {:scopes        [{:scope #{:personal-chats}}]
                                                          :description   :string
                                                          :short-preview :view
                                                          :preview       :view
                                                          :parameters    [{:id           :keyword
                                                                           :type         #{:text :phone :password :number}
                                                                           :placeholder  :string
                                                                           :suggestions? :view}]}}}})

(defn parse [m]
  (reader/parse {:capacities capacities} m))

(def registry (registry/new-registry))

(def id "status")

(defn collectibles []
  (registry/hooks registry 'hooks/status.collectibles))

(defn command-hook->command [[id {:keys [description scopes parameters preview short-preview]}]]
  (reify protocol/Command
    (id [_] (name id))
    (scope [_] (set (map :scope scopes)))
    (description [_] description)
    (parameters [_] parameters)
    (validate [_ _ _])
    (on-send [_ _ _])
    (on-receive [_ _ _])
    (short-preview [_ o] (println "SHORT-PREVIEW" o) (fn [o] (println "......MESSAGE SP" o) (short-preview o)))
    (preview [_ o] (println "PREVIEW" o) (fn [o] (println "......MESSAGE P" (type preview) o) (preview o)))))

(defn chat-commands []
  (map command-hook->command (registry/hooks registry 'hooks/status.chat.commands)))

(try
  (let [{:keys [data errors]} (parse extensions)]
    (when errors
      (println "Failed to parse status extensions" errors))
    (registry/add! registry id data)
    (registry/activate! registry id))
  (catch :default e (println "EXC" e)))
