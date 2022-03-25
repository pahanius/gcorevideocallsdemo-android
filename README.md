# Системные требования

* Минимальная версия android: 4.3 (api level 18).

# Интеграция Video calls SDK

## Импорт Video calls SDK и настройка проекта

1. Создаём новый проект
2. Копируем `GCoreVideoCallsSDK` и `mediasoup-android-client` в папку проекта
3. Добавляем их в ваш проект(Gradle):
   ```groovy
   implementation project(':GCoreVideoCallsSDK')
   implementation project(':mediasoup-android-client')
   ```	

## Инициализация Video calls SDK

1. Инициализируем SDK

   ```kotlin
   GCoreMeet.instance.init(applicationContext: Application, logger: gcore.videocalls.meet.logger.Logger?, enableLogs: Boolean) 
   ```

2. Определяем параметры и методы для коннекта к серверу

   ```kotlin   
   GCoreMeet.instance.clientHostName = "studio.gvideo.co"
   GCoreMeet.instance.roomManager.displayName = "John Snow"
   GCoreMeet.instance.setRoomId("serv01234")
   GCoreMeet.instance.roomManager.peerId = "user09876"
   ```

| Метод| Тип | Описание| |--|--|--| | setRoomId | String | Room ID to connect to<br>*
Example:* `roomId: "serv01234"` |

| Параметр| Тип | Описание| |--|--|--| |displayName | String | Set display name of
participant<br>[Link for extra details in knowledge base](https://gcorelabs.com/support/articles/4404682043665/#h_01FBPQAEZZ1GR7SF7G7TBAYJWZ)<br>*Example:* `displayName: "John Snow"`
| | peerId | String? (optional) | ID of a participant from your internal system. Please specify
userID if you have your own. Or just leave this field blank, then the value will be generated
automatically.<br>[Link for extra details in knowledge base](https://gcorelabs.com/support/articles/4404682043665/#h_01FBPQC18B1E3K58C05A8E81Y7)<br>*Example:* `peerId: "user0000000001"`
| | clientHostName | String? (optional) | В данном параметре клиент передает то доменное имя,
которое он использует для эксплуатации веб версии мита. Значение: только домен без указания
протокола.<br>*Example:* `clientHostName: "meet.gcore.lu"`| | isModer | Boolean | если true, то
пользователь будет модератором, и для него доступен дополнительный функционал

3.Для работы SDK нужны следующие разрешения

   ```xml

<uses-permission android:name="android.permission.INTERNET" /><uses-permission
android:name="android.permission.CALL_PHONE" /><uses-permission
android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```

4. Перед присоединением к комнате нужно подключится
   ```kotlin    
   GCoreMeet.instance.startConnection(context)
    ```

5. Перед подключением к окманте можем установить настройки для аудио и видео
   ```kotlin
   roomManager.options.startWithCam = true
   roomManager.options.startWithMic = true
   ```

6. Присоединение к комнате
   ```kotlin
   roomManager = GCoreMeet.instance.roomManager
   if (roomManager.isClosed()) {
       roomManager.join()
   }
   ```

7. Подписываемся
   ```kotlin   
   ///Изменеие состояния подключения 
   roomManager.roomProvider.connected
   
   ///Список всех пиров в комнате
   roomManager.roomProvider.peers 
   ///или
   GCoreMeet.instance.getPeers()
   
   ///Состояние комнаты
   roomManager.roomProvider.roomInfo.observeForever{ roomInfo->
      roomInfo.activeSpeakerIds   //Разговаривающие в данный момент участники
      roomInfo.connectionState    //Cостояние комнаты             
   }
   
   ///Локальное состояние
   roomManager.roomProvider.me.observeForever { localState: LocalState ->
        
   }
   
    ///Пользователь
    
    ///состояние запроса приглашенияя в комнату, если комната ожидания активирована
    roomManager.roomProvider.waitingState.observeForever{state: WaitingState -> }
    ///может быть 
    ///NOTHING 
    ///IN_WAITING в ожидании ответа модератора
    ///ACCEPTED модератор разрешил вход
    ///REJECTED модератор отклонил вход
    
    ///модератор удалил пользователя из комнаты
    roomManager.roomProvider.closedByModerator.observeForever{}
    
    ///Разрешение на использование микрофона/камеры
    roomManager.roomProvider.micAllowed.observeForever{allowet: Boolean -> }
    roomManager.roomProvider.camAllowed.observeForever{allowet: Boolean -> }
    
    /// Модератор сам запросил включение микро/камеры
    roomManager.roomProvider.acceptedAudioPermission.observeForever{}
    roomManager.roomProvider.acceptedVideoPermission.observeForever{}
    
    /// Модератор подтвердил запрос пользователя на включение 
    roomManager.roomProvider.acceptedAudioPermissionFromModerator.observeForever{}
    roomManager.roomProvider.acceptedVideoPermissionFromModerator.observeForever{}

    ///возникает если у пользователя заблокирован микрофон/камера, но он хочет их включить
    roomManager.askUserConfirmMic.observeForever{}
    roomManager.askUserConfirmCam.observeForever{}
   
    ///Модератор
     
    roomManager.roomProvider.requestToModerator.observeForever { data: RequestPeerData ->
    ///RequestPeerData содержит userName, peerId, requestType 
    ///requestType может быть AUDIO,VIDEO,SHARE запросы микрофона, камеры, демонстрации сответственно
    
    }
   


   
   ```

8. Отображение участников комнаты

   Для отображения видео с камеры устройства используем LocalVideoView
   ```xml
   <gcore.videocalls.meet.ui.view.me.LocalVideoView
   android:id="@+id/localVideo" />
   ```

И инициализируем

   ```kotlin
   localVideo.connect()
   ```

Для отображание пиров других участников используем PeerVideoView

   ```xml

<gcore.videocalls.meet.ui.view.peer.PeerVideoView android:id="@+id/peer_video_view" />
   ```   

   ```kotlin
   peer_video_view.connect(peer.id)
   ```

9. Изменение состояния камеры и микрофона (эти методы доступны и в LocalVideoView)

   ```kotlin
   ///Включение камеры/микрофона
   GCoreMeet.instance.roomManager.enableCam() 
   GCoreMeet.instance.roomManager.enableMic()
   ///Отключение камеры/микрофона 
   GCoreMeet.instance.roomManager.disableCam()
   GCoreMeet.instance.roomManager.disableMic()
   ///Переключение сосотояния камеры/микрофона
   GCoreMeet.instance.roomManager.disableEnableCam()
   GCoreMeet.instance.roomManager.disableEnableMic()
   
   ///Переключение вида камеры 
   roomManager.changeCam()
   ```

10. Функционал пользователя 
    
    Запросить включение микрофона у модератора, используется если модератор запретил его пользователю 
    roomManager.askModeratorEnableMic()
    Аналогично с камерой 
    roomManager.askModeratorEnableCam()

11. Функционал модератора
    
    Модератор может взамиодействовать с пользователем через методы roomManager, в которые передается
    параметр peerId пользователя

    enableAudioByModerator/disableAudioByModerator для зарпоса включения/отключения микрофона
    пользователя enableVideoByModerator/disableVideoByModerator камера
    enableShareByModerator/disableShareByModerator демонстрация
    
    используется для ответа, после события requestToModerator, если к модератору пришел запрос на включение микрофона, камеры или демонстрации
    acceptedPermission(peerData: RequestPeerData)


