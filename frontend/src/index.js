import App from "./App"
import SignUp from "./SignUp"
import SignIn from "./SignIn"

import React from "react"
import ReactDOM from "react-dom"



var appNode = document.getElementById("app")
if(appNode){
  ReactDOM.render(<App />, appNode);
}

var signInNode = document.getElementById("signin")
if(signInNode){
    ReactDOM.render(<SignIn />, signInNode);
}

var signUpNode = document.getElementById("signup")
if(signUpNode){
    ReactDOM.render(<SignUp />, signUpNode);
}