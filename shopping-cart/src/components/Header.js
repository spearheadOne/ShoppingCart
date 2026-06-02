import React from "react";

import Cart from "./Cart"

import "../stylesheets/Header.css"
import { useDispatch } from "react-redux";
import { loginActions } from "../store/login-slice";


function Header(){

    const dispatch = useDispatch();

    function logout(){
       dispatch(loginActions.logout());
    }


   return (
       <header>
           <nav className="header-nav">
               <ul className="header-ul">
                   <li className="header-li">
                       <h2 className="header-h2">
                           Shopping cart
                       </h2>
                   </li>
                   <li className="header-li">
                      <Cart /> 
                   </li>
                   <li>
                       <button onClick={logout} className="header-logout"> Logout</button>
                   </li>
               </ul>
           </nav>
       </header> 
   )
}


export default Header;