import {
  applyMiddleware,
  combineReducers,
  configureStore,
} from "@reduxjs/toolkit";
import { logger } from "redux-logger";
import thunk from "redux-thunk";
import { roomListReducer } from "store/reducers/roomListReducer";
import { subjectsReducer } from "store/reducers/subjectsReducer";
import { roomFilterReducer } from "store/reducers/roomFilterReducer";
import { friendListReducer } from "store/reducers/friendListReducer";
import { wordListReducer } from "./reducers/wordListReducer";
import { logBookmarkListReducer } from "store/reducers/logBookmarkListReducer";
import { logFilterReducer } from "store/reducers/logFilterReducer";
import { logListReducer } from "store/reducers/logListReducer";
import { pageReducer } from "store/reducers/pageReducer";

const rootReducer = combineReducers({
  roomListReducer: roomListReducer.reducer,
  subjectsReducer: subjectsReducer.reducer,
  roomFilterReducer: roomFilterReducer.reducer,
  friendListReducer: friendListReducer.reducer,
  wordListReducer: wordListReducer.reducer,
  logBookmarkListReducer: logBookmarkListReducer.reducer,
  logFilterReducer: logFilterReducer.reducer,
  logListReducer: logListReducer.reducer,
  pageReducer: pageReducer.reducer,
});

export const store = configureStore(
  { reducer: rootReducer },
  applyMiddleware(thunk, logger)
);

export default store;
