import React, { useState } from "react";
import axios from 'axios';
import { Box, TextField, Button } from "@mui/material";
import { XSquare } from "react-bootstrap-icons";

/** @jsxImportSource @emotion/react */
import { css } from "@emotion/react";


// 친구 신청 보내기 axios
const sendFriend = async (fromId, toId, message, setRelation, handleCloseForm) => {
  try {
    const response = await axios.post(
      `${process.env.REACT_APP_HOST}/friend/send`, {
        fromId: fromId,
        toId: toId,
        message: message,
      }
    )
    if (response.status === 200) {
      console.log('send request!')
      console.log(response)
      setRelation(1)
      handleCloseForm()
    }
  } catch (e) {
    console.log(e)
  }
}


function RequestForm({ fromId, toId, handleCloseForm, setRelation }) {
  const style = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: "40%",
    height: "20%",
    bgcolor: "background.paper",
    border: "3px solid #111",
    boxShadow: 24,
    p: "32px 0 200px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  };

  const [text, setText] = useState("우리 친구해요!");

  const onChange = (e) => {
    setText(e.target.value);
  };

  return (
    <Box sx={style}>
      <Box
        sx={{
          display: "flex",
          width: "100%",
          justifyContent: "flex-end",
          mr: "10%",
          mb: "5%",
        }}
      >
        <XSquare
          css={css`
            margin-left: 75%;
            font-size: 30px;
            cursor: pointer;
          `}
          onClick={handleCloseForm}
        />
      </Box>

      <TextField
        multiline
        rows={8}
        sx={{
          width: "70%",
          backgroundColor: "white",
          border: "3px solid #111",
          mt: "10px",
        }}
        value={text}
        inputProps={{ maxLength: 100 }}
        onChange={onChange}
      />


      <Button
        sx={{
          backgroundColor: "#FFC804",
          border: "3px solid #111",
          color: "#111111",
          margin: "30px 0 35px 0",
          width: "30%",
          height: "30%",
        }}
        onClick={() => sendFriend(fromId, toId, text, setRelation, handleCloseForm)}
      >
        친구신청 보내기
      </Button>
    </Box>
  );
}

export default RequestForm;
