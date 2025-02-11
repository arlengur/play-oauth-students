Запускаем приложение `sbt run` (http://localhost:9000)
Нажимаем `Click here to apply this script now!` чтобы создать необходимые таблицы в БД

Для взаимодействия с приложением нужно сначала получить `access token` для известного клиента используя 
clientId, для этого переходим по адресу `http://localhost:9000/auth/authorize?client_id=clientId`

Вводим данные пользователя `user/pass` и получаем `access-token`.

Теперь можно перейти к списку студентов по кнопке `Show students` и выполнять разные операции над ними.

Также можно получить список всех студентов по curl запросу:
`$ curl -H "Authorization: Bearer 4fffd829-d0c3-4a86-8909-c9d5abb9b57f" http://localhost:9000/studentsjson`