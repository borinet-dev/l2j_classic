<table width=290>
    <tr>
        <td><font color="919191">HWID:</font><br1> %hwid%</td>
    </tr>
    <tr>
        <td>
            <table bgcolor=000000 width=270>
                <tr>
                    <td><font color="919191">이름</font></td>
                    <td><font color="919191">계정</font></td>
                    <td><font color="919191">동작</font></td>
                </tr>
                %records%
            </table>
			<br>
        </td>
    </tr>
	<tr>
        <td>
        <a action="bypass -h admin_sg_show %hwid%">새로고침</a>&nbsp;&nbsp;&nbsp;
        <a action="bypass -h admin_sg_kick_session %hwid%">Kick all</a>&nbsp;
        </td>
    </tr>
</table>
<br>
<table>
    <tr>
        <td><font color="LEVEL">Issue ban</font></td>
    </tr>
    <tr>
        <td>
            사유: <edit var="reason" width=265 height=15><br>
			<button value="Ban HWID" action="bypass -h admin_sg_ban hwid %hwid% $reason" width=100 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF">
        </td>
    </tr>
</table>