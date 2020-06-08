import React from "react";
import "./App.css";
import {
  HashRouter as Router,
  Switch,
  Route,
  Redirect,
} from "react-router-dom";
import Result from "./page/Result/Result";
import SearchInput from "./page/Search/Search";
const App = () => (
  <Router>
    <Switch>
      <Route exact path="/search" component={SearchInput} />
      <Route exact path="/result/:name" component={Result} />
      <Redirect to="/search"/>
    </Switch>
  </Router>
);

export default App;
