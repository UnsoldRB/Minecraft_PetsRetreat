package com.unsoldriceball.petsretreat.rods;

import com.unsoldriceball.petsretreat.RetreatUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;




//このクラスはEffect_Rodから使用される。
public class Packet_Rod implements IMessage
{
        private int f_dim;
        private UUID f_uuid_player;
        private Enum_Rod f_useditem;




        //コンストラクタ
        public Packet_Rod()
        {
            // 空のコンストラクタが必要らしい。
        }



        //コンストラクタ2個目(使うのはこっち)
        public Packet_Rod(int dim, UUID uuid_player, Enum_Rod usedItem)
        {
            this.f_dim = dim;
            this.f_uuid_player = uuid_player;
            this.f_useditem = usedItem;
        }



        //パケット送信時に送信元が処理するイベント
        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(f_dim);
            buf.writeLong(f_uuid_player.getMostSignificantBits());
            buf.writeLong(f_uuid_player.getLeastSignificantBits());
            buf.writeInt(f_useditem.ordinal());
        }



        //パケット受信時に送信先が処理するイベント。
        @Override
        public void fromBytes(ByteBuf buf)
        {
            f_dim = buf.readInt();
            long mostSignificantBits = buf.readLong();
            long leastSignificantBits = buf.readLong();
            f_uuid_player = new UUID(mostSignificantBits, leastSignificantBits);
            f_useditem = RetreatUtils.toRetreatEnum(buf.readInt());
        }



        public static class Handler implements IMessageHandler<Packet_Rod, IMessage>
        {
            //パケットを受信したときに発生するイベント(Server Only)
            @Override
            public IMessage onMessage(Packet_Rod message, MessageContext ctx)
            {
                final int _DIM = message.f_dim;
                final UUID _UUID_PLAYER = message.f_uuid_player;
                final EntityPlayer _PLAYER = DimensionManager.getWorld(_DIM).getPlayerEntityByUUID(_UUID_PLAYER);

                if (_PLAYER != null)
                {
                    final Enum_Rod _USEDITEM = message.f_useditem;

                    //以下、使ったアイテムに応じてサーバー側から関数を実行する。
                    // 実行する関数は全てEffect_Rod内にある。
                    if (_USEDITEM == Enum_Rod.ROD_COMMAND)
                    {
                        Effect_Rod.commandRod_TeleportAround(_PLAYER);
                    }
                    else if (_USEDITEM == Enum_Rod.ROD_TRANSFER)
                    {
                        Effect_Rod.transferRod_TeleportPets(_PLAYER);
                    }
                    else if (_USEDITEM == Enum_Rod.ROD_RETREAT)
                    {
                        Effect_Rod.retreatRod_RetreatAround(_PLAYER);
                    }
                }

                return null;
            }
        }
}
