import { useState, useEffect } from "react";
import axios from "axios";

import { Input, Box, Button } from "@mui/material";
import React from "react";
import StarRating from "./StarRating";

// 자기평가 등록 axios
const onEvaluationAxios = async (roomNo, userId, score, description) => {
  try {
    const response = await axios.patch(
      `${process.env.REACT_APP_HOST}/evaluation/self`,
      {
        roomNo,
        userId,
        score,
        description,
      }
    );
    if (response.status === 200) {
      // console.log(response.data);
      return true;
    } else {
      return false;
    }
  } catch (e) {
    console.log(e);
    return false;
  }
};

function SelfEvaluationInput({ roomNo, selfScore, selfDesc }) {
  const [registered, setRegistered] = useState(false);
  const [score, setScore] = useState(0);
  const [description, setDescription] = useState("");

  // props undefined 해결,,
  useEffect(() => {
    setRegistered(selfScore > 0);
    setScore(selfScore);
    setDescription(selfDesc ? selfDesc : "");
  }, [selfScore, selfDesc]);

  const onEvaluationHandler = async (e) => {
    e.preventDefault();
    const res = await onEvaluationAxios(
      roomNo,
      sessionStorage.getItem("userId"),
      Math.max(score, 1),
      description
    );
    setRegistered(res);
  };

  const onChangeDescription = (e) => {
    if (!registered) {
      setDescription(e.target.value);
    }
  };

  return (
    <>
      <Box
        component="form"
        sx={{
          border: "3px solid #111",
          position: "relative",
          width: "100%",
          mt: "15px",
        }}
      >
        <StarRating
          registered={registered}
          score={Math.max(1, score)}
          setScore={setScore}
        />
        <Input
          rows={5}
          multiline={true}
          value={description}
          onChange={onChangeDescription}
          sx={{
            width: "100%",
            backgroundColor: "white",
            padding: "1rem 5.5rem 1rem 1rem",
            fontFamily: "Noto Sans KR",
            fontSize: "16px",
          }}
        />

        {!registered && (
          <Button
            variant="contained"
            component="label"
            sx={{
              position: "absolute",
              bottom: "24px",
              right: "24px",
              border: "3px solid #111",
              borderRadius: "0",
              backgroundColor: "#3C90F2",
              fontFamily: "Noto Sans KR",
              fontSize: "16px",
              color: "white",
              "&:hover": {
                backgroundColor: "#1766C3",
              },
            }}
            onClick={onEvaluationHandler}
          >
            등록
            <input hidden type="submit"></input>
          </Button>
        )}
      </Box>
    </>
  );
}

export default SelfEvaluationInput;
