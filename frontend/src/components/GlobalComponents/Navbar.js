import React, { useState } from "react";
import { NavLink } from "react-router-dom";
/** @jsxImportSource @emotion/react */
import { css } from "@emotion/react";
import NavbarDropdown from "./NavbarDropdown";
import white_logo from "assets/corns_logo.png";


const Navbar = () => {
  const [user, stateUser] = useState(true);

  return (
    <nav
      css={css`
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding: 0 24px;
        width: calc(100% - 250px);
        height: 60px;
        background: #ffc804;
        position: absolute;
        top: 0;
        left: 105px;
      `}
    >
      <div
        css={css`
          flex: none;
          order: 0;
          flex-grow: 0;
          color: 'black'
        `}
      >
        <NavLink to="/">
          <img
            src={white_logo}
            alt="corns-logo"
            css={css`
              height: 2rem;
            `}
          />
        </NavLink>

        {/* 임시 로그인 토글 */}
        <button onClick={() => stateUser(!user)}>임시 로그인</button>
      </div>

      <ul
        css={css`
          list-style: none;
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
          padding: 0px;
          width: 40%;
          height: 26px;
          color: black;
        `}
      >
        <li>
          <NavLink to="/conversation" style={{ textDecoration: "none", color: "black" }} activeStyle={{fontWeight: 'bold'}}>쫑알쫑알</NavLink>
        </li>
        <li>
          <NavLink to="/conversationLog" style={{ textDecoration: "none", color: "black" }} activeStyle={{fontWeight: 'bold'}}>쫑알로그</NavLink>
        </li>
        <li>
          <NavLink to="/growthRecord" style={{ textDecoration: "none", color: "black" }} activeStyle={{fontWeight: 'bold'}}>성장기록</NavLink>
        </li>
        <li>
          <NavLink to="/community/ranking/sincerity" style={{ textDecoration: "none", color: "black" }} activeStyle={{fontWeight: 'bold'}}>커뮤니티</NavLink>
        </li>

        {user ? (
          <>
            <li>
              <NavLink to="/login" style={{ textDecoration: "none", color: "black" }}>로그인</NavLink>
            </li>
            <li>
              <NavLink to="/signin" style={{ textDecoration: "none", color: "black" }}>회원가입</NavLink>
            </li>
          </>
        ) : (
          <>
            <li>
              <NavLink to="/" style={{ textDecoration: "none", color: "black" }}>로그아웃</NavLink>
            </li>
            <li>
              <NavbarDropdown />
            </li>
          </>
        )}
      </ul>
    </nav>
  );
};

export default Navbar;