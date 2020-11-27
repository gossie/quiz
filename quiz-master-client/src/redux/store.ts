import { createStore } from "redux";
import  {handleError } from "./reducers";

export default createStore(handleError);
