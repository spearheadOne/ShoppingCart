import React  from "react";
import { useDispatch } from "react-redux";
import { loginActions } from "../store/login-slice";

import "../stylesheets/Login.css" 


function Login(){

  const dispatch = useDispatch();

  function handleSubmit(event){
    event.preventDefault();
    dispatch(loginActions.login())

  }  

  return(
        <div className="login-container">
        <h1 className="login-h1">Login</h1>
        <form onSubmit={handleSubmit}>
            <label htmlFor="id">Account id</label>
            <input type="text" name="id" id="id"/>
 
            <label htmlFor="password">Password</label>
            <input type="password" name="password" id="password"/>
            <button className="login-btn" type="submit">
                Login
            </button>
        </form>
          
    </div>
  ) 
}


export default Login;