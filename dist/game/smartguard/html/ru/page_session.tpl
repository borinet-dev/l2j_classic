<table>
    <tr>
        <td><font color="919191">HWID:</font> %hwid%</td>
    </tr>
    <tr>
        <td>
            <table bgcolor=000000 width=270>
                <tr>
                    <td><font color="919191">Игрок</font></td>
                    <td><font color="919191">Аккаунт</font></td>
                    <td><font color="919191">Действия</font></td>
                </tr>
                %records%
            </table>
			<br>
        </td>
    </tr>
	<tr>
        <td>
        <a action="bypass -h admin_sg_show %hwid%">Обновить</a>&nbsp;&nbsp;&nbsp;
        <a action="bypass -h admin_sg_kick_session %hwid%">Кикнуть всех</a>&nbsp;
        </td>
    </tr>
</table>
<br>
<table>
    <tr>
        <td><font color="LEVEL">Блокировка</font></td>
    </tr>
    <tr>
        <td>
            Причина: <edit var="reason" width=265 height=15><br>
			<button value="Забанить HWID" action="bypass -h admin_sg_ban hwid %hwid% $reason" width=100 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3">
        </td>
    </tr>
</table>