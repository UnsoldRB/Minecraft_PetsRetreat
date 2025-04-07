package com.unsoldriceball.petsretreat;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

import static com.unsoldriceball.petsretreat.RetreatUtils.randomValue;


//���̃N���X��RetreatUtils����g�p�����B
public class Packet_Particle implements IMessage
{
    private UUID f_uuid_player;
    private int f_id_particle;
    private int f_count;
    private float f_speed;
    private boolean f_spread;
    private float f_lx;
    private float f_ly;
    private float f_lz;




    //�R���X�g���N�^
    public Packet_Particle()
    {
        // ��̃R���X�g���N�^���K�v�炵���B
    }



    //�R���X�g���N�^2��(�g���̂͂�����)
    public Packet_Particle(UUID uuid_player, int id_particle, int count, float speed, boolean spread, float lx, float ly, float lz)
    {
        this.f_uuid_player = uuid_player;
        this.f_id_particle = id_particle;
        this.f_count = count;
        this.f_speed = speed;
        this.f_spread = spread;
        this.f_lx = lx;
        this.f_ly = ly;
        this.f_lz = lz;
    }



    //�p�P�b�g���M���ɑ��M������������C�x���g
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(f_uuid_player.getMostSignificantBits());
        buf.writeLong(f_uuid_player.getLeastSignificantBits());
        buf.writeInt(f_id_particle);
        buf.writeInt(f_count);
        buf.writeFloat(f_speed);
        buf.writeBoolean(f_spread);
        buf.writeFloat(f_lx);
        buf.writeFloat(f_ly);
        buf.writeFloat(f_lz);
    }



    //�p�P�b�g��M���ɑ��M�悪��������C�x���g�B
    @Override
    public void fromBytes(ByteBuf buf)
    {
        long mostSignificantBits = buf.readLong();
        long leastSignificantBits = buf.readLong();
        f_uuid_player = new UUID(mostSignificantBits, leastSignificantBits);
        f_id_particle = buf.readInt();
        f_count = buf.readInt();
        f_speed = buf.readFloat();
        f_spread = buf.readBoolean();
        f_lx = buf.readFloat();
        f_ly = buf.readFloat();
        f_lz = buf.readFloat();
    }




    public static class Handler implements IMessageHandler<Packet_Particle, IMessage>
    {
        //�p�P�b�g����M�����Ƃ��ɔ�������C�x���g(ClientOnly)
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(Packet_Particle message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                final UUID _UUID_PLAYER = message.f_uuid_player;
                final World _WORLD = Minecraft.getMinecraft().world;
                final EntityPlayer _PLAYER = _WORLD.getPlayerEntityByUUID(_UUID_PLAYER);
                final EnumParticleTypes _PARTICLE = EnumParticleTypes.getParticleFromId(message.f_id_particle);

                if (_PLAYER != null && _PARTICLE != null) {
                    final int _COUNT = message.f_count;
                    final float _SPEED = message.f_speed;
                    final boolean _SPREAD = message.f_spread;
                    final float _COMPENSATE_HEIGHT = 0.5f;
                    final float _COMPENSATE_SPEED = 1.75f;
                    final float _X = (float) (_PLAYER.posX - message.f_lx);
                    final float _Y = (float) (_PLAYER.posY - message.f_ly);
                    final float _Z = (float) (_PLAYER.posZ - message.f_lz);

                    //�p�[�e�B�N����count�̐����������B
                    for (int __i = 0; __i <= _COUNT; __i++) {
                        //�p�[�e�B�N���̐����ʒu���g�U�����邩�ǂ����B
                        if (_SPREAD) {
                            final float __COMPENSATE_X = (float) (randomValue(__i, 50) * _SPEED);
                            final float __COMPENSATE_Y = (float) (randomValue(__i + 1, 50) * _SPEED);
                            final float __COMPENSATE_Z = (float) (randomValue(__i + 2, 50) * _SPEED);

                            _WORLD.spawnParticle
                                    (
                                            _PARTICLE,
                                            _X + __COMPENSATE_X,
                                            _Y + __COMPENSATE_Y + _COMPENSATE_HEIGHT,
                                            _Z + __COMPENSATE_Z,
                                            0.0d,
                                            0.0d,
                                            0.0d
                                    );
                        }
                        else
                        {
                            _WORLD.spawnParticle
                                    (
                                            _PARTICLE,
                                            _X,
                                            _Y + _COMPENSATE_HEIGHT,
                                            _Z,
                                            randomValue(__i, 50) * _SPEED,
                                            (Math.abs(randomValue(__i + 1, 50))) * (_SPEED * _COMPENSATE_SPEED),
                                            randomValue(__i + 2, 50) * _SPEED
                                    );
                        }
                    }
                }
            }

            return null;
        }
    }
}
