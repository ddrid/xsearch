import React, { Component } from "react";
import { Input, message } from "antd";
import "./Result.css";
import { List, Avatar, Space } from "antd";
import { RightOutlined, LeftOutlined } from "@ant-design/icons";
import axios from "axios";
import { Link } from "react-router-dom";
const { Search } = Input;

class Result extends Component {
  constructor(props) {
    super(props);
    this.state = {
      reqParams: {
        size: 10,
        from: 0,
        query: "",
      },
      listData: [],
      pageNum: [
        {
          text: "1",
          sel: true,
        },
        {
          text: "2",
          sel: false,
        },
        {
          text: "3",
          sel: false,
        },
        {
          text: "4",
          sel: false,
        },
        {
          text: "5",
          sel: false,
        },
        {
          text: "6",
          sel: false,
        },
        {
          text: "7",
          sel: false,
        },
        {
          text: "8",
          sel: false,
        },
        {
          text: "9",
          sel: false,
        },
        {
          text: "10",
          sel: false,
        },
      ],
    };
  }

  //处理收到的时间戳
  time_stamp(stamp, need_s = false) {
    //no_h   不需要小时和分钟  need_s 需要秒
    let date = new Date(stamp * 1000); //时间戳为10位需*1000，时间戳为13位的话不需乘1000
    var Y = date.getFullYear();
    var M = date.getMonth() + 1;
    var D = date.getDate();
    var h = date.getHours();
    var m = date.getMinutes();
    var s = date.getSeconds();

    if (M < 10) {
      M = "0" + M;
    }
    if (D < 10) {
      D = "0" + D;
    }
    if (h < 10) {
      h = "0" + h;
    }
    if (m < 10) {
      m = "0" + m;
    }
    if (s < 10) {
      s = "0" + s;
    }

    if (need_s) {
      //返回时间带秒
      return Y + "/" + M + "/" + D + " " + h + ":" + m + ":" + s;
    }
    return Y + "/" + M + "/" + D + " " + h + ":" + m;
  }

  componentWillMount() {
    console.log(this.props.match.params.name);
    let { reqParams } = this.state;
    this.setState({
      reqParams: {
        ...reqParams,
        query: this.props.match.params.name,
      },
    });
    axios
      .post("https://230418ad-ea7c-4a40-a773-64cd424f4c4c.mock.pstmn.io/query", {
        params: {
          ...reqParams,
          query: this.props.match.params.name,
        },
      })
      .then((res) => {
        this.setState({
          listData: res.data,
        });
      });
  }

  searchRes = (e) => {
    let { reqParams } = this.state;
    // window.location.href = ''
    window.history.replaceState(null, null, `#/result/query?query=${e}`);
    this.setState({
      ...reqParams,
      query: e,
    });
    axios
      .post("https://230418ad-ea7c-4a40-a773-64cd424f4c4c.mock.pstmn.io/query", {
        params: {
          ...reqParams,
          query: e,
        },
      })
      .then((res) => {
        this.setState({
          listData: res.data,
        });
      });
  };

  pageChange = (text) => {
    let { pageNum, reqParams } = this.state,
      currentIndex,
      new_arr = [];

    for (let i = 0; i < pageNum.length; i++) {
      //点击的同一个
      if (pageNum[i].sel && pageNum[i].text == text) {
        return;
      }
    }

    axios
      .post("https://230418ad-ea7c-4a40-a773-64cd424f4c4c.mock.pstmn.io/query", {
        params: {
          ...reqParams,
          from: text,
        },
      })
      .then((res) => {
        this.setState({
          listData: res.data,
        });
      });

    this.setState({
      reqParams: {
        ...reqParams,
        from: text,
      },
    });
    if (text == 1 || text == 2) {
      //点击的是1
      //只需改变高亮
      for (let i = 0; i < pageNum.length; i++) {
        if (pageNum[i].text == text) {
          new_arr = new_arr.concat([
            {
              text: pageNum[i].text,
              sel: true,
            },
          ]);
        } else {
          new_arr = new_arr.concat([
            {
              text: pageNum[i].text,
              sel: false,
            },
          ]);
        }
      }
      console.log(new_arr);
      this.setState({
        pageNum: JSON.parse(JSON.stringify(new_arr)),
      });
      return;
    }

    for (let i = 0; i < pageNum.length; i++) {
      //当前点击的排第几
      if (pageNum[i].text == text) {
        currentIndex = i;
      }
    }

    if (Math.abs(currentIndex - 5) > 3) {
      //需要全体变换数字
      if (currentIndex - 5 < 0) {
        for (let i = 0; i < pageNum.length; i++) {
          new_arr = new_arr.concat([
            {
              text: pageNum[i].text - 1,
              sel: Number(currentIndex) + 1 === i ? true : false,
            },
          ]);
        }
      } else {
        for (let i = 0; i < pageNum.length; i++) {
          new_arr = new_arr.concat([
            {
              text: Number(pageNum[i].text) + 1,
              sel: currentIndex - 1 === i ? true : false,
            },
          ]);
        }
      }
      this.setState({
        pageNum: new_arr,
      });
      return;
    }

    //只需改变高亮
    for (let i = 0; i < pageNum.length; i++) {
      if (pageNum[i].text === text) {
        new_arr = new_arr.concat([
          {
            text: pageNum[i].text,
            sel: true,
          },
        ]);
      } else {
        new_arr = new_arr.concat([
          {
            text: pageNum[i].text,
            sel: false,
          },
        ]);
      }
    }
    this.setState({
      pageNum: new_arr,
    });
  };

  leftClick = (left) => {
    let { pageNum, reqParams } = this.state;
    if (pageNum[0].text == 1 && left) {
      //是第一个了则停止
      return;
    }
    if (left) {
      for (let i = 0; i < pageNum.length; i++) {
        pageNum[i].text = Number(pageNum[i].text) - 1;
      }
    } else {
      for (let i = 0; i < pageNum.length; i++) {
        pageNum[i].text = Number(pageNum[i].text) + 1;
      }
    }

    this.setState(
      {
        reqParams: {
          ...reqParams,
          from: left ? Number(reqParams.from) - 1 : Number(reqParams.from) + 1,
        },
      },
      () => {
        axios
          .post("https://230418ad-ea7c-4a40-a773-64cd424f4c4c.mock.pstmn.io/query", {
            params: {
              ...this.state.reqParams,
            },
          })
          .then((res) => {
            this.setState({
              listData: res.data,
            });
          });
      }
    );

    this.setState({
      pageNum: JSON.parse(JSON.stringify(pageNum)),
    });
  };
  
  render() {
    let { listData, pageNum, reqParams } = this.state;
    return (
      <div className="res_content">
        <div className="res_wrapper">
          <div className="input_res">
            <Search
              className="input"
              placeholder="请输入搜索内容"
              enterButton
              onSearch={this.searchRes}
              defaultValue={this.props.match.params.name}
            />
          </div>
          <List
            itemLayout="vertical"
            size="large"
            style={{ marginBottom: "40px" }}
            pagination={null}
            dataSource={listData}
            renderItem={(item) => (
              <List.Item
                key={item.id}
                actions={[
                  <a style={{ fontSize: "14px", color: "#1a1a1a", }}>{item.score}</a>,
                  <a href={item.url}>{item.url}</a>,
                  <a style={{ fontSize: "14px", color: "#1a1a1a", }}>更新时间：{this.time_stamp(item.updateTime)}</a>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <a
                      style={{
                        fontSize: "18px",
                        fontWeight: "600",
                        lineHeight: "1.6",
                        color: "#1a1a1a",
                      }}
                      href={item.url}
                    >
                      {item.title}
                    </a>
                  }
                  description={
                    <div
                      style={{
                        fontSize: "16px",
                        color: "#1a1a1a",
                      }}
                      className="key_word"
                      dangerouslySetInnerHTML={{
                        __html: "..." + item.highlight + "...",
                      }}
                    ></div>
                  }
                />
              </List.Item>
            )}
          />
          <div className="page">
            <ul>
              <li
                onClick={() => {
                  this.leftClick(true);
                }}
              >
                <LeftOutlined />
              </li>
              {pageNum.map((item, index) => {
                return (
                  <li
                    className={reqParams.from == item.text ? "sel" : ""}
                    key={item.text}
                    onClick={() => {
                      this.pageChange(item.text, index);
                    }}
                  >
                    {item.text}
                  </li>
                );
              })}
              {/* 5为分界线    到五的时候则 增或者 减*/}
              <li
                onClick={() => {
                  this.leftClick(false);
                }}
              >
                <RightOutlined />
              </li>
            </ul>
          </div>
        </div>
      </div>
    );
  }
}
export default Result;
