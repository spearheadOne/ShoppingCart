import { useSelector, useDispatch } from "react-redux"
import Layout from "./components/Layout";
import { useEffect } from "react";
import Notification from "./components/Notification";

import { fetchCartData, sendCartData } from "./store/cart-action";
import {fetchProducts} from "./store/product-action";

let isFirstRender = true
function App() {

  const cart = useSelector(state => state.cart);
  const dispatch = useDispatch();
  const notification = useSelector(state => state.notification.notification)


   useEffect(()=>{
     dispatch(fetchCartData())
     dispatch(fetchProducts())
   },[dispatch]);


  useEffect(() => {

    if (isFirstRender) {
      isFirstRender = false;
      return;
    }

    if (cart.changed){
      dispatch(sendCartData(cart));
    }
 
  }, [cart,dispatch]);

  return (
    <div>
      {notification && <Notification type={notification.type} message={notification.message} />}
      <Layout />

    </div>
  );

}


export default App;
