import React from "react";

import Cart from "./Cart"

import "../stylesheets/Header.css"


function Header(){
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
               </ul>
           </nav>
       </header> 
   )
}


export default Header;
