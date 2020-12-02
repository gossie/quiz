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

|URL|Possible Status codes|
