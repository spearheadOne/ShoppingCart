import Layout from "./components/Layout";
import { useEffect } from "react";
import Notification from "./components/Notification";
import cartStore from "./store/cart-store";

let isFirstRender = true
function App() {

  const cart = cartStore(state => state.cart);
  const notification = cartStore(state => state.notification);
  const fetchCartData = cartStore(state => state.fetchCartData);
  const fetchProducts = cartStore(state => state.fetchProducts);
  const sendCartData = cartStore(state => state.sendCartData);


   useEffect(()=>{
     fetchCartData()
     fetchProducts()
   },[fetchCartData, fetchProducts]);


  useEffect(() => {

    if (isFirstRender) {
      isFirstRender = false;
      return;
    }

    if (cart.changed){
      sendCartData(cart);
    }
 
  }, [cart, sendCartData]);

  return (
    <div>
      {notification && <Notification type={notification.type} message={notification.message} />}
      <Layout />

    </div>
  );

}


export default App;
