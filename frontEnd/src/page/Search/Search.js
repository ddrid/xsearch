import React, { Component } from "react";
import { Input, message } from "antd";
import "./Search.css";
import img from "./xsearch.png";

const { Search } = Input;
class SearchInput extends Component {
  constructor(props) {
    super(props);
  }

  goRes = (e) => {
    if (!e) {
      message.info("请输入搜索内容");
    }
    this.props.history.push({ pathname: "/result/" + e });
  };

  render() {
    return (
      <div className="search_wrapper">
        <div className="xsearch_icon">
          <img width="820" height="150" src={img} />
        </div>
        <Search
          className="input"
          placeholder="请输入搜索内容"
          enterButton
          onSearch={this.goRes}
        />
      </div>
    );
  }
}
export default SearchInput;
