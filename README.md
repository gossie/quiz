# quiz

## History

Once upon a time, before Covid, a couple of friends an I used to visit an Irish pub to participate in their weekly pub quiz. And to have a drink or two. Obviously that is not possible at the moment. One evening we had a video chat and I just started to ask random questions. The others were shouting their answers at me. It was fun, but we noticed that we need a buzzer.

## Local deployment

To spin up the whole system just run `docker-compose up`. As a result there will be three application running:
* quiz-master-client (http://localhost:3000)
  The application where the Quiz master can create a new quiz, load an existing one, post question and gives points.
* quiz-participan-client (http://localhost:3001)
  The application where a participant can join an existing quiz, sees the current question and the current score of all participants and can use the buzzer.
* quiz-server (http://localhost:5000)
  The backend application that handles all the requests and stores the quiz data.

## API

URL | Method | Possible Status codes | Description
----|--------|-----------------------|------------
/api/quiz | POST | 201 | Creates a new quiz
/api/quiz/{quizId} | PUT | 200 | Reopens the current question of the quiz
/api/quiz/{quizId} | PATCH | 200 | Reveals the participants' answers for the current question 
/api/quiz/{quizId} | POST | 200 | Finishes the quiz and creates final statistics
/api/quiz/{quizId}/undo | DELETE | 200 | Reverts the last action performed on the quiz
/api/quiz/{quizId}/redo | POST | 200 | Perform a redo
/api/quiz/{quizId}/questions | POST | 201, 409 | Creates a new question
/api/quiz/{quizId}/questions/{questionId} | PUT | 201, 409 | The question is edited
/api/quiz/{quizId}/questions/{questionId} | PATCH | 201, 409 | The question is asked
/api/quiz/{quizId}/questions/{questionId} | DELETE | 201, 409 | The question is deleted
/api/quiz/{quizId}/participants | POST | 201, 400, 404, 409 | Creates a new participant
/api/quiz/{quizId}/participants/{participantId} | DELETE | 200, 409 | A participant is removed from the quiz
/api/quiz/{quizId}/participants/{participantId}/answers | POST | 200, 409 | Rates a participant's answer as correct or incorrect
/api/quiz/{quizId}/participants/{participantId}/buzzer | PUT | 200, 409 | A participant has buzzered or has sent a freetext answer
/api/quiz/{quizId}/participants/{participantId}/choices/{choiceId} | PUT | 200, 409 | A participant has selected an option of a multiple choice question
/api/quiz/{quizId}/participants/{participantId}/togglereveal | PUT | 200, 409 | A participant activates or deactivates that his or her answers can be seen by other participants
/api/questionPool | GET | 200 | Returns the questions that were already played, so some can be copied into a new quiz

The application uses server sent events to broadcast actions to all participants of the quiz.

URL | Description
----|------------
/api/quiz/{quizId}/quiz-master | 
/api/quiz/{quizId}/quiz-participant | 
