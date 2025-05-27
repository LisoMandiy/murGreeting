# murGreeting

👋 **murGreeting** — это продвинутый плагин для Spigot-серверов Minecraft, который позволяет полностью настроить приветствия игроков.

## Возможности

- Кастомное сообщение при входе в чат (`join-message`)
- Поддержка цветовых кодов `&`, `§` и `#HEX`
- Приветствие в виде `Title` и `Subtitle` (`title-message`, `subtitle-message`)
- Поддержка PlaceholderAPI (переменные вроде `%player_name%`)
- Приветственный звук (`sound`)
- Полная настройка через конфиг
- Команда `/murgreeting` с автодополнением
- Возможность включать и отключать:
    - чаты
    - титры
    - звуки
    - отправку сообщения в чат
    - системное сообщение входа (joined the game)

## Команды

| Команда                        | Описание                                     |
|-------------------------------|----------------------------------------------|
| `/murgreeting reload`         | Перезагрузка конфига                         |
| `/murgreeting editconfig <ключ> <значение>` | Редактировать конфиг в реальном времени |
| `/gmessages`                  | Включить/выключить системное сообщение входа |

## Поддерживаемые ключи для `editconfig`

- `join-message`
- `title-message`
- `subtitle-message`
- `sound`
- `show-default-join-message`

## Примеры

```yml
join-message: '&6Привет, $player!'
title-message: '&aДобро пожаловать'
subtitle-message: '&7на сервер'
sound: 'ENTITY_PLAYER_LEVELUP'
show-default-join-message: false
"# murGreeting" 
