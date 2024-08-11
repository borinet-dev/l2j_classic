<font color="DBC309">Поиск сессии:</font>
<table height=25>
    <tr>
        <td><center><edit var="query" width=265 height=15></center></td>
    </tr>
</table>
<table>
    <tr>
        <td><button value="Найти по игроку" action="bypass -h admin_sg_find player $query" width=120 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3"></td>
        <td><button value="Найти по HWID" action="bypass -h admin_sg_find hwid $query" width=120 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3"></td>
    </tr>
</table>
<br>

<font color="DBC309">Действия:</font>
<table>
    <tr>
        <td><button value="Обновить настройки" action="bypass -h admin_sg_reload config" width=120 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3"></td>
    </tr>
</table>