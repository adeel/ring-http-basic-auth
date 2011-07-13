(ns ring.middleware.http-basic-auth
  "Ring middleware for basic HTTP authentication."
  (:use [remvee.base64 :as base64]))

(declare *user*)

(defn- get-credentials [req]
  (let [auth ((req :headers) "authorization")
        cred (and auth (base64/decode-str (last (re-find #"^Basic (.*)$" auth))))
        username (and cred (last (re-find #"^(.*):" cred)))
        password (and cred (last (re-find #":(.*)$" cred)))]
    [username password]))

(defn wrap-with-auth
  "Wrap response with a basic authentication challenge as described in
  RFC2617 section 2.

  The authenticate function is called with two parameters, the
  username and password, and should return a value when the login is
  valid."

  [app authenticate]
  (fn [req]
    (let [[username password] (get-credentials req)]
      (binding [*user* (authenticate username password)]
        (app req)))))

(defn wrap-require-auth
  "The realm is a descriptive string visible to the visitor.  It,
  together with the canonical root URL, defines the protected resource
  on the server.

  The denied-response is a ring response structure which will be
  returned when authorization fails.  The appropriate status and
  authentication headers will be merged into it.  It defaults to plain
  text 'access denied' response."

  [app authenticate & [realm denied-response]]
  (fn [req]
    (let [[username password] (get-credentials req)]
      (binding [*user* (authenticate username password)]
        (if *user*
          (app req)
          (assoc
            (merge {:headers {"Content-Type" "text/plain"}
                    :body "HTTP authentication required."}
              denied-response)
            :status  401
            :headers (merge (:headers denied-response)
              {"WWW-Authenticate" (format
                "Basic realm=\"%s\"" (or realm "Restricted Area"))})))))))