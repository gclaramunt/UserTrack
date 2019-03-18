The project uses H2 as an in-memory database and starts it automatically on start

To run the server just do `sbt run`
With curl you can test the service:

curl -X POST 'localhost:8080/analytics?timestamp=1&user=u2&event=click'
curl -X POST 'localhost:8080/analytics?timestamp=2&user=u2&event=click'
curl -X POST 'localhost:8080/analytics?timestamp=2&user=u1&event=impression'
curl -X POST 'localhost:8080/analytics?timestamp=3650&user=u1&event=impression'
curl localhost:8080/analytics?timestamp=1
curl localhost:8080/analytics?timestamp=3603
